package org.knowledger.ledger.service.handles.builder

import org.knowledger.common.database.DatabaseMode
import org.knowledger.common.database.DatabaseType
import org.knowledger.common.database.ManagedDatabase
import org.knowledger.common.database.ManagedSession
import org.knowledger.common.database.orient.OrientDatabase
import org.knowledger.common.database.orient.OrientDatabaseInfo
import org.knowledger.common.hash.AvailableHashAlgorithms
import org.knowledger.common.hash.Hash
import org.knowledger.common.misc.base64Encode
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.PersistenceWrapper
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


    private fun generateDB() {
        if (db == null) {
            db = OrientDatabase(
                OrientDatabaseInfo(
                    dbMode, dbType, path,
                    user = dbUser, password = dbPassword
                )
            )
        }
        if (session == null) {
            session = db?.newManagedSession(base64Encode(hash))
        }
        persistenceWrapper = PersistenceWrapper(hash, session!!)
    }

    private fun attemptToResolveId() =
        //Get ledger params
        persistenceWrapper.getLedgerHandleByHash(hash).mapSuccess {
            hasher = AvailableHashAlgorithms.getHasher(it.ledgerParams.crypter)
            it
        }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        generateDB()
        return attemptToResolveId().mapSuccess {
            ledgerConfig = it
            addToContainers()
            LedgerHandle(this)
        }
    }
}