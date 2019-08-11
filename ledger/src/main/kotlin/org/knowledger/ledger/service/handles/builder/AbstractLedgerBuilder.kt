package org.knowledger.ledger.service.handles.builder

import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.database.DatabaseMode
import org.knowledger.ledger.core.database.DatabaseType
import org.knowledger.ledger.core.database.ManagedDatabase
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.orient.OrientDatabase
import org.knowledger.ledger.core.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.base64Encode
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.PersistenceWrapper
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
    internal var hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER

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

    internal fun buildDB(hash: Hash) {
        if (db == null) {
            db = OrientDatabase(
                OrientDatabaseInfo(
                    dbMode, dbType, path,
                    user = dbUser, password = dbPassword
                )
            )
        }
        if (session == null) {
            session = db?.newManagedSession(hash.base64Encode())
        }
        persistenceWrapper = PersistenceWrapper(hash, session!!)
        persistenceWrapper.registerDefaultSchemas()

    }

    internal fun setCustomDB(
        db: ManagedDatabase,
        session: ManagedSession
    ) {
        this.db = db
        this.session = session
    }

    protected fun addToContainers() {
        val hash = ledgerConfig.ledgerId.hashId
        LedgerHandle.containers[hash.base64Encode()] =
            LedgerContainer(
                hash,
                hasher,
                ledgerConfig.ledgerParams,
                ledgerConfig.coinbaseParams,
                persistenceWrapper,
                DefaultDiff
            )
    }

}