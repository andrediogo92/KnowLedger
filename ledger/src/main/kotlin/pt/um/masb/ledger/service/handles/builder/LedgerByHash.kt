package pt.um.masb.ledger.service.handles.builder

import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.flatMapSuccess
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
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
        db: ManagedDatabase, session: ManagedSession,
        persistenceWrapper: PersistenceWrapper
    ) =
        apply {
            setCustomDB(db, session, persistenceWrapper)
        }

    private fun checkHash(): Outcome<Unit, LedgerHandle.Failure> =
        if (hash != Hash.emptyHash) {
            Outcome.Ok(Unit)
        } else {
            Outcome.Error(
                LedgerHandle.Failure.NoIdentitySupplied
            )
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
            session = db?.newManagedSession(base64Encode(ledgerConfig.ledgerId.hashId))
            persistenceWrapper = PersistenceWrapper(session!!)
        }
    }

    private fun attemptToResolveId() =
        //Get ledger params
        persistenceWrapper.getId(hash).mapSuccess {
            hasher = AvailableHashAlgorithms.getHasher(it.ledgerParams.crypter)
            it
        }

    override fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        return checkHash().flatMapSuccess {
            generateDB()
            attemptToResolveId()
        }.mapSuccess {
            ledgerConfig = it
            addToContainers()
            LedgerHandle(this)
        }
    }
}