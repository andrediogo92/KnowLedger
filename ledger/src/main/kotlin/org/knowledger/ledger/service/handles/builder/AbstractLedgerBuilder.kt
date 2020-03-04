package org.knowledger.ledger.service.handles.builder

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerialModule
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.base.data.DefaultDiff
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.orient.OrientDatabase
import org.knowledger.ledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.serial.with
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.PersistenceWrapper
import java.io.File

abstract class AbstractLedgerBuilder {
    protected abstract var hash: Hash
    protected var db: ManagedDatabase? = null
    protected var session: ManagedSession? = null
    protected var path: String = "./db"
    private var dbMode: DatabaseMode = DatabaseMode.EMBEDDED
    private var dbType: DatabaseType = DatabaseType.LOCAL
    private var dbUser: String = "admin"
    private var dbPassword: String = "admin"
    private val registeredAdapters: MutableSet<AbstractStorageAdapter<*>> =
        mutableSetOf()
    private var serialModule: SerialModule = baseModule
    lateinit var encoder: BinaryFormat

    private lateinit var ledgerConfig: LedgerConfig
    internal lateinit var adapterManager: AdapterManager
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

    internal abstract fun attemptToResolveId(): Outcome<LedgerConfig, LedgerHandle.Failure>

    private fun buildDB(hash: Hash) {
        if (db == null) {
            db = OrientDatabase(
                OrientDatabaseInfo(
                    dbMode, dbType, path,
                    user = dbUser, password = dbPassword
                )
            )
        }
        if (session == null) {
            session = db!!.newManagedSession(hash.base64Encoded())
        }
        adapterManager = AdapterManager(ledgerInfo, registeredAdapters)
        persistenceWrapper = PersistenceWrapper(
            hash, session!!, adapterManager
        )
    }

    protected fun setCustomDB(
        db: ManagedDatabase,
        session: ManagedSession
    ) {
        this.db = db
        this.session = session
    }

    protected fun registerAdapters(
        types: Iterable<AbstractStorageAdapter<out LedgerData>>
    ) {
        registeredAdapters.addAll(types)
    }


    private fun registerSerializers() {
        val serial = registeredAdapters.map {
            //this is a valid cast because serializer pair classes force
            //construction of properties with correct LedgerData upper
            //type bound.
            @Suppress("UNCHECKED_CAST")
            it.clazz as Class<LedgerData> with it.serializer as KSerializer<LedgerData>
        }.toTypedArray()
        serialModule = serialModule.withLedger(serial)
    }

    private fun buildInfo(): LedgerInfo =
        LedgerInfo(
            ledgerId = ledgerConfig.ledgerId,
            hasher = hasher,
            ledgerParams = ledgerConfig.ledgerParams,
            coinbaseParams = ledgerConfig.coinbaseParams,
            serialModule = serialModule,
            formula = DefaultDiff,
            encoder = encoder
        )

    fun build(): Outcome<LedgerHandle, LedgerHandle.Failure> {
        registerSerializers()
        encoder = Cbor(UpdateMode.UPDATE, true, serialModule)
        return attemptToResolveId().mapSuccess {
            ledgerConfig = it
            ledgerInfo = buildInfo()
            buildDB(hash)
            adapterManager.initChainHandle(ledgerInfo, persistenceWrapper)
            persistenceWrapper.registerDefaultSchemas()
            LedgerHandle(this)
        }
    }
}