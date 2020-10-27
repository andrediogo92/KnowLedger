package org.knowledger.ledger.chain.handles.builder

import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import org.knowledger.ledger.adapters.service.LedgerMagicPair
import org.knowledger.ledger.chain.handles.LedgerHandle
import org.knowledger.ledger.chain.results.LedgerBuilderFailure
import org.knowledger.ledger.chain.results.intoHandle
import org.knowledger.ledger.chain.results.tryOrHandleUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.storage.LedgerId
import org.knowledger.ledger.storage.results.LoadFailure
import java.io.File

/**
 * A fluent builder class for [LedgerHandle] that builds
 * by attempting to retrieve a previously registered
 * ledger with [hash] from the supplied database.
 *
 */
internal data class LedgerByHash(
    override var hash: Hash,
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByHash> {
    override fun withDBPath(path: File): Outcome<LedgerByHash, LedgerBuilderFailure> =
        setDBPath(path).map { this }

    override fun withDBPath(path: String): LedgerByHash =
        apply { this.path = path }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType, dbUser: String?, dbPassword: String?,
    ): LedgerByHash =
        apply { setCustomSession(dbOpenMode, dbSessionType, dbUser, dbPassword) }

    override fun withCustomDB(db: ManagedDatabase): LedgerByHash =
        apply { this.db = db }

    internal fun withCustomDB(db: ManagedDatabase, session: ManagedSession): LedgerByHash =
        apply { setCustomDB(db, session) }

    private fun getLedgerHandleByHash(hash: Hash): Outcome<LedgerId, LedgerBuilderFailure> =
        tryOrHandleUnknownFailure {
            val adapter = defaultLedgerAdapters.ledgerIdStorageAdapter
            val magicPair = LedgerMagicPair(
                defaultLedgerAdapters.ledgerParamsStorageAdapter, factories.ledgerParamsFactory
            )
            val query = UnspecificQuery(
                """SELECT 
                    FROM ${adapter.id}
                    WHERE hash = :hash
                """.trimIndent(), mapOf("hash" to hash.bytes)
            )
            session!!.query(query).let { result ->
                if (result.hasNext()) {
                    adapter
                        .load(hash, result.next().element, magicPair)
                        .mapError(LoadFailure::intoHandle)
                } else {
                    LedgerBuilderFailure.NonExistentLedger.err()
                }
            }
        }


    override fun attemptToResolveId(): Outcome<LedgerId, LedgerBuilderFailure> =
        //Get ledger params
        getLedgerHandleByHash(hash).onSuccess {
            hashers = Hashers.getHasher(it.ledgerParams.hashers)
        }
}