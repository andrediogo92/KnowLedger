package pt.um.masb.ledger.storage.transactions

import mu.KLogging
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.ManagedSchemas
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericQuery
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.database.query.SimpleBinaryOperator
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.common.storage.adapters.Loadable
import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.results.collapse
import pt.um.masb.ledger.results.tryOrDataUnknownFailure
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.results.tryOrQueryUnknownFailure
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.adapters.IdentityStorageAdapter
import pt.um.masb.ledger.service.adapters.ServiceLoadable
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.adapters.BlockHeaderStorageAdapter
import pt.um.masb.ledger.storage.adapters.BlockStorageAdapter
import pt.um.masb.ledger.storage.adapters.CoinbaseStorageAdapter
import pt.um.masb.ledger.storage.adapters.QueryLoadable
import pt.um.masb.ledger.storage.adapters.StorageLoadable
import pt.um.masb.ledger.storage.adapters.TransactionOutputStorageAdapter
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
import java.security.PublicKey


/**
 * A Thread-safe wrapper into a DB context
 * for the ledger library.
 */
class PersistenceWrapper(
    private val session: ManagedSession
) {
    private val schemas = session.managedSchemas
    private lateinit var schemasRegistered: List<String>
    internal val isClosed
        get() = session.isClosed

    init {
        registerDefaultSchemas()
    }

    private fun registerDefaultSchemas(
    ) {
        val schemas = setOf<SchemaProvider<out Any>>(
            //ServiceAdapters
            ChainHandleStorageAdapter,
            //StorageAdapters
            IdentityStorageAdapter,
            BlockHeaderStorageAdapter,
            BlockStorageAdapter,
            CoinbaseStorageAdapter,
            TransactionOutputStorageAdapter,
            TransactionStorageAdapter,
            BlockParamsStorageAdapter,
            LedgerIdStorageAdapter,
            LedgerParamsStorageAdapter,
            MerkleTreeStorageAdapter,
            PhysicalDataStorageAdapter,
            //DataAdapters
            DummyDataStorageAdapter
        )
        schemasRegistered = schemas.map { it.id }
        schemas.forEach {
            registerSchema(
                it
            )
        }
    }

    fun registerSchema(
        schemaProvider: SchemaProvider<out Any>
    ): PersistenceWrapper =
        apply {
            if (!schemas.hasSchema(schemaProvider.id)) {
                createSchema(
                    schemas,
                    schemaProvider
                )
            } else {
                replaceSchema(
                    schemas,
                    schemaProvider
                )
            }
        }

    private fun createSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider<out Any>
    ) {
        val cl = schema.createSchema(provider.id)
        cl?.let {
            provider.properties.forEach {
                cl.createProperty(it.key, it.value)
            }
        }
    }


    private fun replaceSchema(
        schema: ManagedSchemas,
        provider: SchemaProvider<out Any>
    ) {
        val cl = schema.getSchema(provider.id)
        cl?.let {
            val (propsIn, propsNotIn) = cl
                .declaredPropertyNames()
                .partition {
                    it in provider.properties.keys
                }

            if (propsNotIn.isNotEmpty()) {
                //Drop properties that no longer exist in provider.
                propsNotIn.forEach {
                    cl.dropProperty(it)
                }
            }

            if (propsIn.size != provider.properties.keys.size) {
                //New properties are those in provider that are not already present.
                provider
                    .properties
                    .keys
                    .asSequence()
                    .filter {
                        it !in propsIn
                    }.forEach {
                        cl.createProperty(
                            it,
                            provider.properties.getValue(it)
                        )
                    }
            }
        }
    }

    internal fun closeCurrentSession(): PersistenceWrapper =
        apply {
            session.close()
        }

    fun getInstanceSession(): NewInstanceSession = session

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-typeId that implements [BlockChainData].
     *
     * Returns an [Outcome] with a possible [DataFailure].
     */
    private fun <T : BlockChainData> queryUniqueResult(
        query: GenericQuery,
        loader: Loadable<T>
    ): Outcome<T, DataFailure> =
        tryOrDataUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(res.next().element)
            } else {
                Outcome.Error<T, DataFailure>(
                    DataFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
            }
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [StorageLoadable] to load ledger domain elements from
     * the first applicable database element. [StorageLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     * Returns an [Outcome] with a possible [LoadFailure].
     */
    private fun <T : LedgerContract> queryUniqueResult(
        ledgerHash: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<T, LoadFailure> =
        tryOrLoadUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(
                    ledgerHash,
                    res.next().element
                )
            } else {
                Outcome.Error<T, LoadFailure>(
                    LoadFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
            }
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ServiceLoadable] to load ledger service objects
     * from a database element.
     * [ServiceLoadable]s apply *exclusively*
     * to [ServiceHandle] classes.
     *
     *
     * *Note:* An extra argument is required for any
     * query over a [ServiceLoadable]:
     * - The common wrapper itself.
     *
     * Returns an [Outcome] with a possible [LedgerFailure].
     */
    private fun <T : ServiceHandle> queryUniqueResult(
        ledgerHash: Hash,
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<T, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(
                    this, res.next().element
                )
            } else {
                Outcome.Error<T, LedgerFailure>(
                    LedgerFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
            }
        }


    /**
     * Requires:
     * - The [session] in which to execute the query.
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [QueryLoadable] to load an arbitrary typeId
     * through application of a reduction to the
     * underlying database element.
     *
     *
     * Returns an [Outcome] with a possible [QueryFailure].
     */
    private fun <T : Any> queryUniqueResult(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): Outcome<T, QueryFailure> =
        tryOrQueryUnknownFailure {
            val res = session.query(
                query.query,
                query.params
            )
            if (res.hasNext()) {
                loader.load(res.next().element)
            } else {
                Outcome.Error<T, QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Empty ResultSet for ${query.query}"
                    )
                )
            }
        }


    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-typeId that implements [BlockChainData].
     *
     *
     * Returns an [Outcome] with a possible [DataFailure]
     * over a [Sequence].
     */
    private fun <T : BlockChainData> queryResults(
        query: GenericQuery,
        loader: Loadable<T>
    ): Outcome<Sequence<T>, DataFailure> =
        tryOrDataUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(it.element)
                }.collapse()
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [StorageLoadable] to load ledger domain elements from
     * the first applicable database element. [StorageLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     *
     * Returns an [Outcome] with a possible [LoadFailure]
     * over a [Sequence].
     */
    private fun <T : LedgerContract> queryResults(
        ledgerHash: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): Outcome<Sequence<T>, LoadFailure> =
        tryOrLoadUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(ledgerHash, it.element)
                }.collapse()
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ServiceLoadable] to load ledger service objects
     * from a database element.
     * [ServiceLoadable]s apply *exclusively*
     * to [ServiceHandle] classes.
     *
     *
     * *Note:* One extra argument is required for any
     * query over a [ServiceLoadable]:
     * - The common wrapper itself.
     *
     *
     * Returns an [Outcome] with a possible [LedgerFailure]
     * over a [Sequence].
     */
    private fun <T : ServiceHandle> queryResults(
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): Outcome<Sequence<T>, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(this, it.element)
                }.collapse()
        }


    /**
     * Not to be used directly.
     * Requires knowledge of inner workings of DB.
     *
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [QueryLoadable] to transform the element into a
     * usable typeId.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [Sequence].
     */
    private fun <T : Any> queryResults(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): Outcome<Sequence<T>, QueryFailure> =
        tryOrQueryUnknownFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(it.element)
                }.collapse()
        }


    /**
     * Should be called when querying is finished to
     * reset the persistence context.
     */
    internal fun clearTransactionsContext() =
        closeCurrentSession()


    /**
     * Persists an [element] to an active [ManagedSession]
     * in a synchronous manner.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [StorageID].
     */
    @Synchronized
    internal fun <T : Any> persistEntity(
        element: T,
        storable: Storable<T>
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem)
            if (r != null) {
                Outcome.Ok<StorageID, QueryFailure>(r.identity)
            } else {
                Outcome.Error<StorageID, QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.print()}"
                    )
                )
            }
        }

    /**
     * Persists an [element] to an active [ManagedSession]
     * in a synchronous manner.
     *
     * Returns an [Outcome] with a possible [QueryFailure]
     * over a [StorageID].
     */
    @Synchronized
    fun <T : Any> persistEntity(
        element: T,
        storable: Storable<T>,
        cluster: String
    ): Outcome<StorageID, QueryFailure> =
        tryOrQueryUnknownFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem, cluster)
            if (r != null) {
                Outcome.Ok<StorageID, QueryFailure>(r.identity)
            } else {
                Outcome.Error<StorageID, QueryFailure>(
                    QueryFailure.NonExistentData(
                        "Failed to save element ${elem.print()}"
                    )
                )
            }
        }


    // ------------------------------
    // Blockheader transactions.
    //
    // ------------------------------


    internal fun getBlockHeaderByHash(
        ledgerHash: Hash,
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        BlockHeaderStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "hashId",
                    "hashId",
                    hash
                ),
                it
            )
        }


    internal fun getBlockHeaderByBlockHeight(
        ledgerHash: Hash,
        height: Long
    ): Outcome<BlockHeader, LoadFailure> =
        BlockHeaderStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE, "blockheight",
                    "blockheight",
                    height
                ),
                it
            )

        }


    internal fun getBlockHeaderByPrevHeaderHash(
        ledgerHash: Hash,
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        BlockHeaderStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "previousHash",
                    "hashId",
                    hash
                ),
                it
            )

        }

    internal fun getLatestBlockHeader(
        ledgerHash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        BlockHeaderStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id,
                    "max(blockheight), *"
                ),
                it
            )
        }


    // ------------------------------
    // Block transactions.
    //
    // ------------------------------


    internal fun getBlockByBlockHeight(
        ledgerHash: Hash,
        blockheight: Long
    ): Outcome<Block, LoadFailure> =
        BlockStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "header.blockheight",
                    "blockheight",
                    blockheight
                ),
                it
            )

        }


    internal fun getBlockByHeaderHash(
        ledgerHash: Hash,
        hash: Hash
    ): Outcome<Block, LoadFailure> =
        BlockStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "header.hashId",
                    "hashId",
                    hash
                ),
                it
            )

        }


    internal fun getBlockByPrevHeaderHash(
        ledgerHash: Hash,
        hash: Hash
    ): Outcome<Block, LoadFailure> =
        BlockStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "header.previousHash",
                    "hashId",
                    hash
                ),
                it
            )

        }


    internal fun getLatestBlock(
        ledgerHash: Hash
    ): Outcome<Block, LoadFailure> =
        BlockStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withProjection(
                    "max(header.blockheight), *"
                ),
                it
            )
        }

    fun getBlockListByBlockHeightInterval(
        ledgerHash: Hash,
        startInclusive: Long,
        endInclusive: Long
    ): Outcome<Sequence<Block>, LoadFailure> =
        BlockStorageAdapter.let {
            queryResults(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withBetweenFilter(
                    Filters.WHERE,
                    "header.blockheight",
                    Pair("start", "end"),
                    Pair(startInclusive, endInclusive),
                    SimpleBinaryOperator.AND
                ),
                it
            )
        }


    // ------------------------------
    // Identity transaction.
    //
    // ------------------------------


    internal fun getIdent(id: String): StorageElement? =
        session.query(
            "SELECT * FROM ident WHERE id = :id",
            mapOf(
                "id" to id
            )
        ).let {
            if (it.hasNext()) {
                it.next().element
            } else {
                null
            }
        }


    // ------------------------------
    // Transactions over transactions.
    // Go figure.
    //
    // Execution must be runtime determined.
    // ------------------------------


    fun getTransactionsFromAgent(
        ledgerHash: Hash,
        publicKey: PublicKey
    ): Outcome<Sequence<Transaction>, LoadFailure> =
        TransactionStorageAdapter.let {
            queryResults(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "publicKey",
                    "publicKey",
                    publicKey.encoded
                ),
                it
            )
        }

    fun getTransactionByHash(
        ledgerHash: Hash,
        hash: Hash
    ): Outcome<Transaction, LoadFailure> =
        TransactionStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "hashId",
                    "hashId",
                    hash.bytes
                ),
                it
            )

        }


    //Execution must be runtime determined.
    fun getTransactionsOrderedByTimestamp(
        ledgerHash: Hash
    ): Outcome<Sequence<Transaction>, LoadFailure> =
        TransactionStorageAdapter.let {
            queryResults(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.ORDER,
                    "data.seconds DESC, data.nanos DESC"
                ),
                it
            )

        }

    fun getTransactionsByClass(
        ledgerHash: Hash,
        typeName: String
    ): Outcome<Sequence<Transaction>, LoadFailure> =
        TransactionStorageAdapter.let {
            queryResults(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "data.data.@class",
                    "typeName",
                    typeName
                ),
                it
            )

        }


    //-------------------------
    // LedgerHandle Transactions
    //-------------------------
    fun <T> getChainHandle(
        ledgerHash: Hash,
        clazz: Class<in T>
    ): Outcome<ChainHandle, LedgerFailure> =
        ChainHandleStorageAdapter.let {
            queryUniqueResult(
                ledgerHash,
                GenericSelect(
                    it.id
                ).withSimpleFilter(
                    Filters.WHERE,
                    "clazz",
                    "clazz",
                    clazz.name
                ),
                it
            )
        }

    fun tryAddChainHandle(
        chainHandle: ChainHandle
    ): Outcome<StorageID, QueryFailure> =
        persistEntity(
            chainHandle,
            ChainHandleStorageAdapter
        )

    fun getKnownChainHandleTypes(
    ): Outcome<Sequence<String>, QueryFailure> =
        queryResults(
            GenericSelect(
                ChainHandleStorageAdapter.id
            ).withProjection(
                "clazz"
            ),
            object : QueryLoadable<String> {
                override fun load(
                    element: StorageElement
                ): Outcome<String, QueryFailure> =
                    Outcome.Ok(
                        element.getStorageProperty("clazz")
                    )
            }
        )

    fun getKnownChainHandleIDs(
    ): Outcome<Sequence<StorageID>, QueryFailure> =
        queryResults(
            GenericSelect(
                ChainHandleStorageAdapter.id
            ),
            object : QueryLoadable<StorageID> {
                override fun load(
                    element: StorageElement
                ): Outcome<StorageID, QueryFailure> =
                    Outcome.Ok(element.identity)
            }
        )


    fun getKnownChainHandles(
    ): Outcome<Sequence<ChainHandle>, LedgerFailure> =
        ChainHandleStorageAdapter.let {
            queryResults(
                GenericSelect(
                    it.id
                ),
                it
            )
        }

    fun getId(
        hash: Hash
    ): Outcome<LedgerId, LoadFailure> =
        LedgerIdStorageAdapter.let {
            queryUniqueResult(
                hash,
                GenericSelect(
                    it.id
                ),
                it
            )
        }

    companion object : KLogging()
}
