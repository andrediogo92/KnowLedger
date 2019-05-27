package pt.um.masb.ledger.service

import mu.KLogging
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.DataFormula
import pt.um.masb.common.data.DefaultDiff
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.common.results.Failable
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.io.File
import java.security.KeyPair


/**
 * Create a geographically unbounded ledger.
 */
class LedgerHandle internal constructor(
    builder: Builder
) : ServiceHandle {
    private val pw: PersistenceWrapper = builder.persistenceWrapper!!
    val ledgerId: LedgerId = builder.ledgerId!!
    val isClosed: Boolean
        get() = pw.isClosed

    fun close() {
        pw.closeCurrentSession()
    }

    //TODO: efficiently retrieve chains registered for this ledger.
    val knownChainTypes: Outcome<Sequence<String>, QueryFailure>
        get() = pw.getKnownChainHandleTypes()

    internal val knownChainIDs: Outcome<Sequence<StorageID>, QueryFailure>
        get() = pw.getKnownChainHandleIDs()

    val knownChains: Outcome<Sequence<ChainHandle>, LedgerFailure>
        get() = pw.getKnownChainHandles()

    fun getIdentById(id: String): Identity? {
        val ident: StorageElement? = pw.getIdent(id)
        return if (ident != null) {
            val keyPair = KeyPair(
                stringToPublicKey(ident.getStorageProperty("publicKey")),
                stringToPrivateKey(ident.getStorageProperty("privateKey"))
            )
            Identity(id, keyPair)
        } else {
            null
        }
    }

    fun addStorageAdapter(
        adapter: AbstractStorageAdapter<out BlockChainData>
    ): Boolean =
        dataAdapters.add(adapter).also {
            pw.registerSchema(adapter)
        }


    fun <T : BlockChainData> getChainHandleOf(
        clazz: Class<in T>
    ): Outcome<ChainHandle, LedgerFailure> =
        pw.getChainHandle(
            ledgerId.hashId,
            clazz
        )

    fun <T : BlockChainData> registerNewChainHandleOf(
        clazz: Class<out T>,
        adapter: AbstractStorageAdapter<out T>
    ): Outcome<ChainHandle, LedgerFailure> =
        ChainHandle(
            clazz.name, ledgerId.hashId
        ).let {
            addStorageAdapter(adapter)
            pw.tryAddChainHandle(it).mapToNew(
                {
                    Outcome.Error<ChainHandle, LedgerFailure>(
                        this.failure.intoLedger()
                    )
                }, {
                    Outcome.Ok(it)
                }
            )
        }


    class Builder {
        private var ledgerParams: LedgerParams? = null
        private var coinbaseParams: CoinbaseParams? = null
        private var db: ManagedDatabase? = null
        private var session: ManagedSession? = null
        private var path: String = "./db"
        private var dbMode: DatabaseMode = DatabaseMode.EMBEDDED
        private var dbType: DatabaseType = DatabaseType.LOCAL
        private var dbUser: String = "admin"
        private var dbPassword: String = "admin"

        internal var persistenceWrapper: PersistenceWrapper? = null
        internal var ledgerId: LedgerId? = null
        internal var identity: String = ""
        internal var hash: Hash = Hash.emptyHash

        fun withDBPath(path: File): Outcome<Builder, Failure> =
            if (!path.exists() || path.isDirectory) {
                this.path = path.path
                Outcome.Ok(this)
            } else {
                Outcome.Error(
                    Failure.PathCannotResolveAsDirectory(
                        """Path resolution error:
                            |   ${path.path} as directory: ${path.isDirectory}
                            |   ${path.path} exists: ${path.exists()}
                            """.trimMargin()
                    )
                )
            }

        fun withDBPath(path: String): Builder =
            apply {
                this.path = path
            }

        fun withLedgerIdentity(ledgerId: String): Outcome<Builder, Failure> =
            if (ledgerId == "") {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                hash = Hash.emptyHash
                identity = ledgerId
                Outcome.Ok(this)
            }

        fun byHashRetrieval(hash: Hash): Outcome<Builder, Failure> =
            if (hash == Hash.emptyHash) {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                identity = ""
                this.hash = hash
                Outcome.Ok(this)
            }

        fun withCustomParams(ledgerParams: LedgerParams): Builder =
            apply {
                this.ledgerParams = ledgerParams
            }

        fun withCoinbaseParams(coinbaseParams: CoinbaseParams): Builder =
            apply {
                this.coinbaseParams = coinbaseParams
            }


        fun withCustomSession(
            dbOpenMode: DatabaseMode,
            dbSessionType: DatabaseType,
            dbUser: String?,
            dbPassword: String?
        ): Builder =
            apply {
                dbMode = dbOpenMode
                dbType = dbSessionType
                dbUser?.let {
                    this.dbUser = it
                }
                dbPassword?.let {
                    this.dbPassword = it
                }
            }

        fun withCustomDB(
            db: ManagedDatabase
        ): Builder =
            apply {
                this.db = db
            }

        internal fun withCustomDB(
            db: ManagedDatabase,
            session: ManagedSession,
            pw: PersistenceWrapper
        ): Builder =
            apply {
                this.db = db
                this.session = session
                persistenceWrapper = pw
            }

        fun build(): Outcome<LedgerHandle, Failure> =
            generateLedgerParams().mapResult {
                attemptToResolveId()
            }.mapSuccess {
                generateDB()
                Outcome.Ok<LedgerHandle, Failure>(
                    LedgerHandle(this@Builder)
                )
            }

        private fun generateLedgerParams(): Outcome.Ok<Unit, Failure> =
            Outcome.Ok<Unit, Failure>(Unit).also {
                if (ledgerParams == null) {
                    ledgerParams = LedgerParams()
                }
                if (coinbaseParams == null) {
                    coinbaseParams = CoinbaseParams()
                }
            }


        private fun attemptToResolveId(): Outcome<Unit, Failure> =
            if (hash == Hash.emptyHash && identity == "") {
                Outcome.Error(
                    Failure.NoIdentitySupplied
                )
            } else {
                if (identity != "") {
                    ledgerId = LedgerId(identity, ledgerParams!!)
                }
                Outcome.Ok(Unit)
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
                if (ledgerId == null && hash != Hash.emptyHash) {
                    session = db!!.newManagedSession(base64Encode(hash))
                    persistenceWrapper = PersistenceWrapper(session!!)
                    ledgerId = persistenceWrapper!!.getId(hash).unwrap()
                } else {
                    session = db!!.newManagedSession(base64Encode(ledgerId!!.hashId))
                    persistenceWrapper = PersistenceWrapper(session!!)
                }
            } else {
                if (ledgerId == null && hash != Hash.emptyHash) {
                    ledgerId = persistenceWrapper!!.getId(hash).unwrap()
                }
            }
            containers[base64Encode(ledgerId!!.hashId)] = LedgerContainer(
                ledgerId!!.hashId,
                AvailableHashAlgorithms.getHasher(
                    ledgerId!!.params.crypter
                ),
                ledgerParams!!,
                coinbaseParams!!,
                persistenceWrapper!!,
                DefaultDiff
            )

        }
    }

    sealed class Failure : Failable {
        data class PathCannotResolveAsDirectory(
            override val cause: String
        ) : Failure()

        object NoIdentitySupplied : Failure() {
            override val cause: String
                get() = "No hash or identity supplied to builder."
        }
    }


    companion object : KLogging() {
        private val dataAdapters =
            mutableSetOf<AbstractStorageAdapter<out BlockChainData>>(
                DummyDataStorageAdapter
            )

        private val containers =
            mutableMapOf<String, LedgerContainer>()

        fun getStorageAdapter(
            dataName: String
        ): AbstractStorageAdapter<out BlockChainData>? =
            dataAdapters.find {
                it.id == dataName
            }

        fun getStorageAdapter(
            clazz: Class<out BlockChainData>
        ): AbstractStorageAdapter<out BlockChainData>? =
            dataAdapters.find {
                it.clazz == clazz
            }

        fun getContainer(hash: Hash): LedgerContainer? =
            containers[base64Encode(hash)]


        fun getHasher(ledgerHash: Hash): Hasher? =
            containers[base64Encode(ledgerHash)]?.hasher

        fun getFormula(ledgerHash: Hash): DataFormula? =
            containers[base64Encode(ledgerHash)]?.formula
    }
}
