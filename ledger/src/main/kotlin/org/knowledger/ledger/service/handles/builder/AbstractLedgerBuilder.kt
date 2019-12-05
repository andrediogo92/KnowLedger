package org.knowledger.ledger.service.handles.builder

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.modules.SerialModule
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.orient.OrientDatabase
import org.knowledger.ledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.service.LedgerInfo
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
    protected var serialModule: SerialModule = baseModule
    lateinit var encoder: BinaryFormat

    internal lateinit var ledgerConfig: LedgerConfig
    internal lateinit var persistenceWrapper: PersistenceWrapper
    internal lateinit var ledgerInfo: LedgerInfo
    internal var hasher: Hashers = DEFAULT_HASHER

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
            session = db?.newManagedSession(hash.base64Encoded())
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
        ledgerInfo = LedgerInfo(
            ledgerId = ledgerConfig.ledgerId,
            hasher = hasher,
            ledgerParams = ledgerConfig.ledgerParams,
            coinbaseParams = ledgerConfig.coinbaseParams,
            serialModule = serialModule,
            persistenceWrapper = persistenceWrapper,
            formula = DefaultDiff,
            encoder = encoder
        )
        val hash = ledgerConfig.ledgerId.hash
        LedgerHandle.containers[hash] = ledgerInfo
    }

    @PublishedApi
    internal var iSerialModule: SerialModule
        get() = serialModule
        set(value) {
            serialModule = value
        }

}