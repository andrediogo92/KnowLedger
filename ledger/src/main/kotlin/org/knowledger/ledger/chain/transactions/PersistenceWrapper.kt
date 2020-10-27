package org.knowledger.ledger.chain.transactions

import com.github.michaelbull.result.onSuccess
import org.knowledger.ledger.adapters.QueryLoadable
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.chain.solver.StorageSolver
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.toTag
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSchemas
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.results.LoadFailure


/**
 * A Thread-safe wrapper into a DB context
 * for a ledger.
 */
internal class PersistenceWrapper(
    ledgerHash: Hash, session: ManagedSession, context: PersistenceContext, solver: StorageSolver,
) : AbstractQueryManager(ledgerHash, session, context, solver) {

    private val schemas = session.managedSchemas

    internal fun registerDefaultSchemas() {
        providers.forEach(::registerSchema)
    }

    internal fun registerSchema(schemaProvider: SchemaProvider): PersistenceWrapper =
        apply {
            if (!schemas.hasSchema(schemaProvider.id)) {
                createSchema(schemas, schemaProvider)
            } else {
                replaceSchema(schemas, schemaProvider)
            }
        }

    private fun createSchema(schema: ManagedSchemas, provider: SchemaProvider) {
        schema.createSchema(provider.id)?.let { sch ->
            provider.properties.forEach {
                sch.createProperty(it.key, it.value)
            }
        }
    }


    private fun replaceSchema(schema: ManagedSchemas, provider: SchemaProvider) {
        schema.getSchema(provider.id)?.let { sch ->
            val (propsIn, propsNotIn) =
                sch.declaredPropertyNames().partition(provider.properties.keys::contains)

            if (propsNotIn.isNotEmpty()) {
                //Drop properties that no longer exist in provider.
                propsNotIn.forEach(sch::dropProperty)
            }

            if (propsIn.size != provider.properties.keys.size) {
                //New properties are those in provider that are not already present.
                provider.properties.keys.filterNot(propsIn::contains).forEach {
                    sch.createProperty(it, provider.properties.getValue(it))
                }
            }
        }
    }

    internal fun closeCurrentSession() {
        session.close()
    }

    internal fun chainManager(chainId: ChainId): QueryManager =
        QueryManager(chainId, session, context, solver)

    //-------------------------
    // LedgerHandle Transactions
    //-------------------------
    internal fun getChainHandle(rawTag: Hash): Outcome<ChainHandle, LoadFailure> =
        getChainHandle(rawTag.toTag())

    internal fun getChainHandle(tag: Tag): Outcome<ChainHandle, LoadFailure> =
        chainHandleStorageAdapter.let { adapter ->
            val query = UnspecificQuery(
                """ SELECT 
                    FROM ${adapter.id} 
                    WHERE id.tag = :id
                """.trimIndent(), mapOf("id" to tag.id)
            )
            queryUniqueResult(query, adapter).onSuccess { ch ->
                ch.addQueryManager(chainManager(ch.chainId))
            }
        }


    internal fun tryAddChainHandle(chainHandle: ChainHandle): Outcome<Unit, QueryFailure> {
        return persistEntity(chainHandle, chainHandleStorageAdapter)
    }

    internal fun getKnownChainHandleTypes(): Outcome<List<Tag>, LoadFailure> {
        val query = UnspecificQuery(
            """ SELECT chainId.tag as tag 
                FROM ${chainHandleStorageAdapter.id}
            """.trimIndent()
        )
        val loadable = object : QueryLoadable<Tag> {
            override fun load(
                ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
            ): Outcome<Tag, LoadFailure> =
                element.getStorageProperty<Hash>("tag").toTag().ok()
        }
        return queryResults(query, loadable)
    }

    internal fun getKnownChainHandleIDs(): Outcome<List<StorageID>, LoadFailure> {
        val query = UnspecificQuery(
            """
                SELECT 
                FROM ${chainHandleStorageAdapter.id}
            """.trimIndent()
        )
        val loadable = object : QueryLoadable<StorageID> {
            override fun load(
                ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
            ): Outcome<StorageID, LoadFailure> =
                element.identity.ok()
        }
        return queryResults(query, loadable)
    }


    internal fun getKnownChainHandles(): Outcome<List<ChainHandle>, LoadFailure> =
        chainHandleStorageAdapter.let { adapter ->
            val query = UnspecificQuery(
                """ SELECT 
                    FROM ${adapter.id}
                """.trimIndent()
            )
            queryResults(query, adapter)
        }
}
