package org.knowledger.ledger.service.transactions

import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.database.ManagedSchemas
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.storage.adapters.QueryLoadable


/**
 * A Thread-safe wrapper into a DB context
 * for a ledger.
 */
internal class PersistenceWrapper(
    ledgerHash: Hash,
    session: ManagedSession,
    adapterManager: AdapterManager
) : AbstractQueryManager(ledgerHash, session, adapterManager),
    EntityStore, ServiceClass {

    private val schemas = session.managedSchemas

    internal fun registerDefaultSchemas(
    ) {
        allSchemaProviders.forEach {
            registerSchema(
                it
            )
        }
    }

    internal fun registerSchema(
        schemaProvider: SchemaProvider
    ): PersistenceWrapper =
        apply {
            if (!schemas.hasSchema(schemaProvider.id)) {
                createSchema(
                    schemas,
                    schemaProvider
                )
            } else {
                replaceSchema(
                    schemas,
                    schemaProvider
                )
            }
        }

    private fun createSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider
    ) {
        val cl = schema.createSchema(provider.id)
        cl?.let {
            provider.properties.forEach {
                cl.createProperty(it.key, it.value)
            }
        }
    }


    private fun replaceSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider
    ) {
        val cl = schema.getSchema(provider.id)
        cl?.let {
            val (propsIn, propsNotIn) =
                cl.declaredPropertyNames().partition {
                    it in provider.properties.keys
                }

            if (propsNotIn.isNotEmpty()) {
                //Drop properties that no longer exist in provider.
                propsNotIn.forEach(cl::dropProperty)
            }

            if (propsIn.size != provider.properties.keys.size) {
                //New properties are those in provider that are not already present.
                provider
                    .properties
                    .keys
                    .filter { it !in propsIn }
                    .forEach {
                        cl.createProperty(
                            it,
                            provider.properties.getValue(it)
                        )
                    }
            }
        }
    }

    internal fun closeCurrentSession(): PersistenceWrapper =
        apply {
            session.close()
        }

    internal fun chainManager(chainHash: Hash): QueryManager =
        QueryManager(ledgerHash, session, adapterManager, chainHash)

    //-------------------------
    // LedgerHandle Transactions
    //-------------------------
    internal fun getChainHandle(
        id: Tag
    ): Outcome<ChainHandle, LedgerFailure> =
        getChainHandle(id.base64Encoded())

    internal fun getChainHandle(
        id: String
    ): Outcome<ChainHandle, LedgerFailure> =
        chainHandleStorageAdapter.let {
            queryUniqueResult(
                UnspecificQuery(
                    """
                    SELECT 
                    FROM ${it.id} 
                    WHERE id.tag = :id
                """.trimIndent(),
                    mapOf("id" to id)
                ),
                it
            )
        }


    internal fun tryAddChainHandle(
        chainHandle: ChainHandle
    ): Outcome<StorageID, QueryFailure> {
        return persistEntity(
            chainHandle,
            chainHandleStorageAdapter
        )
    }

    internal fun getKnownChainHandleTypes(
    ): Outcome<Sequence<String>, QueryFailure> =
        queryResults(
            UnspecificQuery(
                """
                SELECT id.tag as tag 
                FROM ${chainHandleStorageAdapter.id}
            """.trimIndent()
            ),
            object : QueryLoadable<String> {
                override fun load(
                    element: StorageElement
                ): Outcome<String, QueryFailure> =
                    Outcome.Ok(
                        element.getStorageProperty("tag")
                    )
            }
        )

    internal fun getKnownChainHandleIDs(
    ): Outcome<Sequence<StorageID>, QueryFailure> =
        queryResults(
            UnspecificQuery(
                """
                SELECT 
                FROM ${chainHandleStorageAdapter.id}
            """.trimIndent()
            ),
            object : QueryLoadable<StorageID> {
                override fun load(
                    element: StorageElement
                ): Outcome<StorageID, QueryFailure> =
                    Outcome.Ok(element.identity)
            }
        )


    internal fun getKnownChainHandles(
    ): Outcome<Sequence<ChainHandle>, LedgerFailure> =
        chainHandleStorageAdapter.let {
            queryResults(
                UnspecificQuery(
                    """
                    SELECT 
                    FROM ${it.id}
                """.trimIndent()
                ),
                it
            )
        }
}
