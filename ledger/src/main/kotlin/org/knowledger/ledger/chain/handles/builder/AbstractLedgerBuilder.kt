package org.knowledger.ledger.chain.handles.builder

import com.github.michaelbull.result.map
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedList
import org.knowledger.database.orient.OrientDatabase
import org.knowledger.database.orient.OrientDatabaseInfo
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.adapters.LedgerAdaptersProvider
import org.knowledger.ledger.chain.LedgerInfo
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.handles.LedgerHandle
import org.knowledger.ledger.chain.results.LedgerBuilderFailure
import org.knowledger.ledger.chain.service.LedgerConfigurationService
import org.knowledger.ledger.chain.service.LedgerService
import org.knowledger.ledger.chain.solver.StorageSolverImpl
import org.knowledger.ledger.chain.transactions.PersistenceWrapper
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.serial.with
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.DefaultDiff
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.LedgerId
import org.tinylog.kotlin.Logger
import java.io.File
import kotlin.reflect.KClass

@OptIn(ExperimentalSerializationApi::class)
internal abstract class AbstractLedgerBuilder {
    internal val service: LedgerService = LedgerConfigurationService
    internal val factories: Factories = LedgerConfigurationService.factories
    internal val defaultLedgerAdapters: LedgerAdaptersProvider =
        LedgerConfigurationService.ledgerAdapters
    protected abstract var hash: Hash
    protected var db: ManagedDatabase? = null
    protected var session: ManagedSession? = null
    protected var path: String = "./db"
    private var dbMode: DatabaseMode = DatabaseMode.EMBEDDED
    private var dbType: DatabaseType = DatabaseType.LOCAL
    private var dbUser: String = "admin"
    private var dbPassword: String = "admin"
    private var serialModule: SerializersModule = baseModule
    lateinit var encoder: BinaryFormat

    internal lateinit var persistenceContext: PersistenceContext
    internal lateinit var persistenceWrapper: PersistenceWrapper
    internal lateinit var ledgerInfo: LedgerInfo
    internal var hashers: Hashers = DEFAULT_HASHER
    internal var formula: DataFormula = DefaultDiff(hashers)

    fun setDBPath(path: File): Outcome<Unit, LedgerBuilderFailure> =
        if (!path.exists() || path.isDirectory) {
            this.path = path.path
            Unit.ok()
        } else {
            LedgerBuilderFailure.PathCannotResolveAsDirectory(
                """Path resolution error:
                    |   ${path.path} as directory: ${path.isDirectory}
                    |   ${path.path} exists: ${path.exists()}
                """.trimMargin()
            ).err()
        }

    fun setCustomSession(
        dbOpenMode: DatabaseMode, dbSessionType: DatabaseType, dbUser: String?, dbPassword: String?,
    ) {
        dbMode = dbOpenMode
        dbType = dbSessionType
        dbUser?.let { this.dbUser = it }
        dbPassword?.let { this.dbPassword = it }
    }

    internal abstract fun attemptToResolveId(): Outcome<LedgerId, LedgerBuilderFailure>

    private fun buildDB(
        registeredAdapters: MutableSortedList<AbstractStorageAdapter<*>>, hash: Hash,
    ) {
        if (db == null) {
            db = OrientDatabase(OrientDatabaseInfo(dbMode, dbType, path, dbUser, dbPassword))
        }
        if (session == null) {
            session = db!!.newManagedSession(hash.base64Encoded())
        }
        persistenceContext = PersistenceContext(
            ledgerInfo, registeredAdapters, factories, defaultLedgerAdapters
        )
        persistenceWrapper = PersistenceWrapper(
            hash, session!!, persistenceContext, StorageSolverImpl(session!!)
        )
    }

    protected fun setCustomDB(db: ManagedDatabase, session: ManagedSession) {
        this.db = db
        this.session = session
    }

    private fun registerSerializers(
        registeredAdapters: MutableSortedList<AbstractStorageAdapter<*>>,
    ) {
        val serial = registeredAdapters.map {
            //this is a valid cast because serializer pair classes force
            //construction of properties with correct LedgerData upper
            //type bound.
            @Suppress("UNCHECKED_CAST")
            it.clazz as KClass<LedgerData> with it.serializer as KSerializer<LedgerData>
        }.toTypedArray()
        serialModule = serialModule.withLedger(serial)
    }

    fun build(): Outcome<LedgerHandle, LedgerBuilderFailure> {
        Logger.debug { "Building Ledger Handle" }
        val registeredAdapters =
            service.calculateAdapters(hashers).toMutableSortedList()
        registerSerializers(registeredAdapters)
        encoder = Cbor {
            encodeDefaults = true
            serializersModule = serialModule
        }
        Logger.debug { "Setup serialization for known data types" }
        return attemptToResolveId().map {
            Logger.debug { "Resolved LedgerId: Hash -> ${it.tag}" }
            ledgerInfo = LedgerInfo(it, hashers, serialModule, factories, formula, encoder)
            Logger.debug { "Initializing Database" }
            buildDB(registeredAdapters, hash)
            Logger.debug { "Registering default schemas " }
            persistenceWrapper.registerDefaultSchemas()
            Logger.debug { "Ledger constructed" }
            LedgerHandle(this)
        }
    }
}