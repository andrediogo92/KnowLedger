package pt.um.masb.ledger.service.handles.builder

import pt.um.masb.common.data.DefaultDiff
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.LedgerContainer
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.io.File

abstract class AbstractLedgerBuilder {
    protected var db: ManagedDatabase? = null
    protected var session: ManagedSession? = null
    protected var path: String = "./db"
    protected var dbMode: DatabaseMode = DatabaseMode.EMBEDDED
    protected var dbType: DatabaseType = DatabaseType.LOCAL
    protected var dbUser: String = "admin"
    protected var dbPassword: String = "admin"

    internal lateinit var ledgerConfig: LedgerConfig
    internal lateinit var persistenceWrapper: PersistenceWrapper
    internal var hasher: Hasher = AvailableHashAlgorithms.Blake2b256Hasher

    fun setDBPath(path: File): Outcome<Unit, LedgerHandle.Failure> =
        if (!path.exists() || path.isDirectory) {
            this.path = path.path
            Outcome.Ok(Unit)
        } else {
            Outcome.Error(
                LedgerHandle.Failure.PathCannotResolveAsDirectory(
                    """Path resolution error:
                            |   ${path.path} as directory: ${path.isDirectory}
                            |   ${path.path} exists: ${path.exists()}
                            """.trimMargin()
                )
            )
        }

    fun setCustomSession(
        dbOpenMode: DatabaseMode,
        dbSessionType: DatabaseType,
        dbUser: String?,
        dbPassword: String?
    ) {
        dbMode = dbOpenMode
        dbType = dbSessionType
        dbUser?.let {
            this.dbUser = it
        }
        dbPassword?.let {
            this.dbPassword = it
        }
    }

    internal fun setCustomDB(
        db: ManagedDatabase,
        session: ManagedSession,
        pw: PersistenceWrapper
    ) {
        this.db = db
        this.session = session
        persistenceWrapper = pw
    }

    protected fun addToContainers() {
        LedgerHandle.containers[base64Encode(ledgerConfig.ledgerId.hashId)] =
            LedgerContainer(
                ledgerConfig.ledgerId.hashId,
                hasher,
                ledgerConfig.ledgerParams,
                ledgerConfig.coinbaseParams,
                persistenceWrapper,
                DefaultDiff
            )
    }

}