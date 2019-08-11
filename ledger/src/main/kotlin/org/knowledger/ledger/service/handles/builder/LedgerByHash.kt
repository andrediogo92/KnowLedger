package org.knowledger.ledger.service.handles.builder

import org.knowledger.ledger.core.database.DatabaseMode
import org.knowledger.ledger.core.database.DatabaseType
import org.knowledger.ledger.core.database.ManagedDatabase
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.hash.AvailableHashAlgorithms
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.handles.LedgerHandle
import java.io.File

data class LedgerByHash(
    internal var hash: Hash
) : AbstractLedgerBuilder(), LedgerBuilder<LedgerByHash> {

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

    override fun withCustomDB(db: ManagedDatabase): LedgerByHash =
        apply {
            this.db = db
        }

    internal fun withCustomDB(
        db: ManagedDatabase, session: ManagedSession
    ) =
        apply {
            setCustomDB(db, session)
        }


    private fun attemptToResolveId(): Outcome<LedgerConfig, LedgerHandle.Failure> =
        //Get ledger params
        persistenceWrapper.getLedgerHandleByHash(hash).mapSuccess {
            hasher = AvailableHashAlgorithms.getHasher(it.ledgerParams.crypter)
            it
        }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        buildDB(hash)
        return attemptToResolveId().mapSuccess {
            ledgerConfig = it
            addToContainers()
            LedgerHandle(this)
        }
    }
}