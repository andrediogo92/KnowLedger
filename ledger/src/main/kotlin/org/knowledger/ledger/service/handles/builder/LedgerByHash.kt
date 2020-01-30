package org.knowledger.ledger.service.handles.builder

import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

/**
 * A fluent builder class for [LedgerHandle] that builds
 * by attempting to retrieve a previously registered
 * ledger with [hash] from the supplied database.
 *
 * Defaults
 */
data class LedgerByHash(
    override var hash: Hash
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByHash> {
    override fun withTypeStorageAdapters(
        types: Iterable<AbstractStorageAdapter<out LedgerData>>
    ): LedgerByHash = apply {
        registerAdapters(types)
    }

    override fun withDBPath(
        path: File
    ): Outcome<LedgerByHash, LedgerHandle.Failure> =
        setDBPath(path).mapSuccess {
            this
        }

    override fun withDBPath(path: String): LedgerByHash =
        apply {
            this.path = path
        }

    override fun withCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType,
        dbUser: String?, dbPassword: String?
    ): LedgerByHash = apply {
        setCustomSession(
            dbOpenMode, dbSessionType,
            dbUser, dbPassword
        )
    }

    override fun withCustomDB(
        db: ManagedDatabase
    ): LedgerByHash =
        apply {
            this.db = db
        }

    internal fun withCustomDB(
        db: ManagedDatabase, session: ManagedSession
    ): LedgerByHash =
        apply {
            setCustomDB(db, session)
        }

    private fun getLedgerHandleByHash(
        hash: Hash
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        session!!.query(
            UnspecificQuery(
                """
                    SELECT 
                    FROM ${LedgerConfigStorageAdapter.id}
                    WHERE hashId = :hashId
                """.trimIndent(),
                mapOf(
                    "hashId" to hash.bytes
                )
            )
        ).let {
            if (it.hasNext()) {
                LedgerConfigStorageAdapter.load(
                    hash, it.next().element
                )
            } else {
                Outcome.Error(
                    LedgerHandle.Failure.NonExistentLedger
                )
            }
        }


    override fun attemptToResolveId(): Outcome<LedgerConfig, LedgerHandle.Failure> =
        //Get ledger params
        getLedgerHandleByHash(hash).mapSuccess {
            hasher = Hashers.getHasher(it.ledgerParams.hasher)
            it
        }
}