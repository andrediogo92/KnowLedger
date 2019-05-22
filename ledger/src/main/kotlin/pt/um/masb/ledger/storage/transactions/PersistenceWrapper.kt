package pt.um.masb.ledger.storage.transactions

import mu.KLogging
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.ManagedSchemas
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageID
import pt.um.masb.common.database.query.ClusterSelect
import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericQuery
import pt.um.masb.common.database.query.SimpleBinaryOperator
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.common.storage.adapters.Loadable
import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.common.storage.results.DataListResult
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.common.storage.results.QueryResult
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.results.*
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.adapters.LedgerHandleStorageAdapter
import pt.um.masb.ledger.service.adapters.ServiceLoadable
import pt.um.masb.ledger.service.results.LedgerListResult
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
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

    init {
        registerDefaultSchemas()
    }

    private fun registerDefaultSchemas(
    ) {
        val schemas = listOf<SchemaProvider<out Any>>(
            BlockHeaderStorageAdapter(),
            BlockStorageAdapter(),
            CoinbaseStorageAdapter(),
            TransactionOutputStorageAdapter(),
            TransactionStorageAdapter(),
            BlockParamsStorageAdapter(),
            LedgerIdStorageAdapter(),
            LedgerParamsStorageAdapter(),
            DummyDataStorageAdapter(),
            MerkleTreeStorageAdapter(),
            PhysicalDataStorageAdapter()
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
     * a usable user-type that implements [BlockChainData].
     *
     * Returns a [DataResult].
     */
    private fun <T : BlockChainData> queryUniqueResult(
        query: GenericQuery,
        loader: Loadable<T>
    ): DataResult<T> =
        tryOrDataQueryFailure {
            session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                loader.load(it.element)
            } ?: DataResult.NonExistentData(
                "Empty ResultSet for ${query.query}"
            )
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [DefaultLoadable] to load ledger domain elements from
     * the first applicable database element. [DefaultLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     *
     * *Note:* One extra argument is required for any query
     * over a [DefaultLoadable]:
     * - A [LedgerId]'s hashId
     *
     *
     * Returns a [LoadResult].
     */
    private fun <T : LedgerContract> queryUniqueResult(
        blockChainId: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): LoadResult<T> =
        tryOrLoadQueryFailure {
            session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                loader.load(blockChainId, it.element)
            } ?: LoadResult.NonExistentData(
                "Empty ResultSet for ${query.query}"
            )
        }

    /**
     * Requires:
     * - The [crypterHash] to identify which crypter
     * to attempt to grab for hashing operations.
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ChainLoadable] to load ledger service objects
     * from a database element.
     * [ChainLoadable]s apply *exclusively*
     * to [ServiceHandle] classes.
     *
     *
     * *Note:* Two extra arguments are required for any
     * query over a [ChainLoadable]:
     * - A [crypterHash] to identify which crypter
     * to attempt to grab for hashing operations.
     * - The common wrapper itself.
     *
     * Returns a [LedgerResult].
     */
    private fun <T : ServiceHandle> queryUniqueResult(
        crypterHash: Hash,
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): LedgerResult<T> =
        tryOrLedgerQueryFailure {
            session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                loader.load(this, crypterHash, it.element)
            } ?: LedgerResult.NonExistentData(
                "Empty ResultSet for ${query.query}"
            )
        }


    /**
     * Requires:
     * - The [session] in which to execute the query.
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [QueryLoadable] to load an arbitrary type
     * through application of a reduction to the
     * underlying database element.
     *
     *
     * Returns a [QueryResult].
     */
    private fun <T : Any> queryUniqueResult(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): QueryResult<T> =
        tryOrQueryQueryFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .firstOrNull()
                ?.let {
                    loader.load(it.element)
                } ?: QueryResult.NonExistentData<T>(
                "Empty ResultSet for ${query.query}"
            )
        }


    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-type that implements [BlockChainData].
     *
     *
     * Returns a [DataListResult] over a list.
     */
    private fun <T : BlockChainData> queryResults(
        query: GenericQuery,
        loader: Loadable<T>
    ): DataListResult<T> =
        tryOrDataListQueryFailure {
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
     * - A [DefaultLoadable] to load ledger domain elements from
     * the first applicable database element. [DefaultLoadable]s
     * apply *exclusively* to [LedgerContract] classes.
     *
     *
     * *Note:* One extra argument is required for any query
     * over a [DefaultLoadable]:
     * - A [LedgerId]'s hashId
     *
     * Returns a [LoadListResult].
     */
    private fun <T : LedgerContract> queryResults(
        blockChainId: Hash,
        query: GenericQuery,
        loader: StorageLoadable<T>
    ): LoadListResult<T> =
        tryOrLoadListQueryFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(blockChainId, it.element)
                }.collapse()
        }

    /**
     * Requires:
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [ChainLoadable] to load ledger service objects
     * from a database element.
     * [ChainLoadable]s apply *exclusively*
     * to [ServiceHandle] classes.
     *
     *
     * *Note:* Two extra arguments are required for any
     * query over a [ChainLoadable]:
     * - A [crypterHash] to identify which crypter
     * to attempt to grab for hashing operations.
     * - The common wrapper itself.
     *
     *
     * Returns a [LedgerListResult].
     */
    private fun <T : ServiceHandle> queryResults(
        crypterHash: Hash,
        query: GenericQuery,
        loader: ServiceLoadable<T>
    ): LedgerListResult<T> =
        tryOrLedgerListQueryFailure {
            session
                .query(query.query, query.params)
                .asSequence()
                .map {
                    loader.load(this, crypterHash, it.element)
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
     * usable type.
     *
     * Returns a [QueryResult] over a [List] of [T].
     */
    private fun <T : Any> queryResults(
        query: GenericQuery,
        loader: QueryLoadable<T>
    ): QueryResult<List<T>> =
        tryOrQueryQueryFailure {
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
     * Returns a [QueryResult] over a [StorageID].
     */
    @Synchronized
    internal fun <T : Any> persistEntity(
        element: T,
        storable: Storable<T>
    ): QueryResult<StorageID> =
        tryOrQueryQueryFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem)
            if (r != null) {
                QueryResult.Success(r.identity)
            } else {
                QueryResult.NonExistentData<StorageID>(
                    "Failed to save element ${elem.print()}"
                )
            }
        }

    /**
     * Persists an [element] to an active [ManagedSession]
     * in a synchronous manner.
     *
     * Returns a [QueryResult] over a [StorageID].
     */
    @Synchronized
    fun <T : Any> persistEntity(
        element: T,
        storable: Storable<T>,
        cluster: String
    ): QueryResult<StorageID> =
        tryOrQueryQueryFailure {
            val elem = storable.store(element, session)
            val r = session.save(elem, cluster)
            if (r != null) {
                QueryResult.Success(r.identity)
            } else {
                QueryResult.NonExistentData<StorageID>(
                    "Failed to save element ${elem.print()}"
                )
            }
        }


// ------------------------------
// Blockchain transactions.
//
// ------------------------------

    /**
     *
     */
    internal fun getBlockChain(
        blockChainId: LedgerId
    ): LedgerResult<LedgerHandle> =
        queryUniqueResult(
            blockChainId.params.crypter.id,
            ClusterSelect(
                "LedgerHandle",
                blockChainId.hashId
            ).withSimpleFilter(
                Filters.WHERE,
                "ledgerHash.hashId",
                "hashId",
                blockChainId.hashId
            ),
            LedgerHandleStorageAdapter()
        )

    internal fun getBlockChain(
        crypterHash: Hash,
        hash: Hash
    ): LedgerResult<LedgerHandle> =
        queryUniqueResult(
            crypterHash,
            ClusterSelect(
                "LedgerHandle",
                hash
            ).withSimpleFilter(
                Filters.WHERE,
                "ledgerHash.hashId",
                "hashId",
                hash
            ),
            LedgerHandleStorageAdapter()
        )


    // ------------------------------
// Blockheader transactions.
//
// ------------------------------
    internal fun getBlockHeaderByHash(
        blockChainId: Hash,
        hash: Hash
    ): LoadResult<BlockHeader> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "BlockHeader",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "hashId",
                "hashId",
                hash
            ),
            BlockHeaderStorageAdapter()
        )


    internal fun getBlockHeaderByBlockHeight(
        blockChainId: Hash,
        height: Long
    ): LoadResult<BlockHeader> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "BlockHeader",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE, "blockheight",
                "blockheight",
                height
            ),
            BlockHeaderStorageAdapter()
        )


    internal fun getBlockHeaderByPrevHeaderHash(
        blockChainId: Hash,
        hash: Hash
    ): LoadResult<BlockHeader> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "BlockHeader",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "previousHash",
                "hashId",
                hash
            ),
            BlockHeaderStorageAdapter()
        )

    internal fun getLatestBlockHeader(
        blockChainId: Hash
    ): LoadResult<BlockHeader> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "BlockHeader",
                blockChainId,
                "max(blockheight), *"
            ),
            BlockHeaderStorageAdapter()
        )


    // ------------------------------
    // Block transactions.
    //
    // ------------------------------


    internal fun getBlockByBlockHeight(
        blockChainId: Hash,
        blockheight: Long
    ): LoadResult<Block> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "Block",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "header.blockheight",
                "blockheight",
                blockheight
            ),
            BlockStorageAdapter()
        )


    internal fun getBlockByHeaderHash(
        blockChainId: Hash,
        hash: Hash
    ): LoadResult<Block> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "Block",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "header.hashId",
                "hashId",
                hash
            ),
            BlockStorageAdapter()
        )


    internal fun getBlockByPrevHeaderHash(
        blockChainId: Hash,
        hash: Hash
    ): LoadResult<Block> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "Block",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "header.previousHash",
                "hashId",
                hash
            ),
            BlockStorageAdapter()
        )


    internal fun getLatestBlock(
        blockChainId: Hash
    ): LoadResult<Block> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "Block",
                blockChainId
            ).withProjection(
                "max(header.blockheight), *"
            ),
            BlockStorageAdapter()
        )

    fun getBlockListByBlockHeightInterval(
        startInclusive: Long,
        endInclusive: Long,
        blockChainId: Hash
    ): LoadListResult<Block> =
        queryResults(
            blockChainId,
            ClusterSelect(
                "Block",
                blockChainId
            ).withBetweenFilter(
                Filters.WHERE,
                "header.blockheight",
                Pair("start", "end"),
                Pair(startInclusive, endInclusive),
                SimpleBinaryOperator.AND
            ),
            BlockStorageAdapter()
        )


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
        blockChainId: Hash,
        publicKey: PublicKey
    ): LoadListResult<Transaction> =
        queryResults(
            blockChainId,
            ClusterSelect(
                "Transaction",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "publicKey",
                "publicKey",
                publicKey.encoded
            ),
            TransactionStorageAdapter()
        )

    fun getTransactionByHash(
        blockChainId: Hash,
        hash: Hash
    ): LoadResult<Transaction> =
        queryUniqueResult(
            blockChainId,
            ClusterSelect(
                "Transaction",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "hashId",
                "hashId",
                hash
            ),
            TransactionStorageAdapter()
        )


    //Execution must be runtime determined.
    fun getTransactionsOrderedByTimestamp(
        blockChainId: Hash
    ): LoadListResult<Transaction> =
        queryResults(
            blockChainId,
            ClusterSelect(
                "Transaction",
                blockChainId
            ).withSimpleFilter(
                Filters.ORDER,
                "data.seconds DESC, data.nanos DESC"
            ),
            TransactionStorageAdapter()
        )

    fun getTransactionsByClass(
        blockChainId: Hash,
        typeName: String
    ): LoadListResult<Transaction> =
        queryResults(
            blockChainId,
            ClusterSelect(
                "Transaction",
                blockChainId
            ).withSimpleFilter(
                Filters.WHERE,
                "data.data.@class",
                "typeName",
                typeName
            ),
            TransactionStorageAdapter()
        )


    //-------------------------
    // LedgerHandle Transactions
    //-------------------------
    fun <T> getChainHandle(
        crypterHash: Hash,
        clazz: Class<T>,
        ledgerHash: Hash
    ): LedgerResult<ChainHandle> =
        queryUniqueResult(
            crypterHash,
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ).withSimpleFilter(
                Filters.WHERE,
                "clazz",
                "clazz",
                clazz.name
            ),
            ChainHandleStorageAdapter()
        )

    fun tryAddChainHandle(
        chainHandle: ChainHandle
    ): QueryResult<StorageID> =
        persistEntity(chainHandle, ChainHandleStorageAdapter())

    fun getKnownChainHandleTypes(
        ledgerHash: Hash
    ): QueryResult<List<String>> =
        queryResults(
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ).withProjection(
                "clazz"
            ),
            object : QueryLoadable<String> {
                override fun load(element: StorageElement): QueryResult<String> =
                    QueryResult.Success(element.getStorageProperty("clazz"))
            }
        )

    fun getKnownChainHandleIDs(
        ledgerHash: Hash
    ): QueryResult<List<StorageID>> =
        queryResults<StorageID>(
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ),
            object : QueryLoadable<StorageID> {
                override fun load(element: StorageElement): QueryResult<StorageID> =
                    QueryResult.Success(element.identity)
            }
        )


    fun getKnownChainHandles(
        crypterHash: Hash,
        ledgerHash: Hash
    ): LedgerListResult<ChainHandle> =
        queryResults(
            crypterHash,
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ),
            ChainHandleStorageAdapter()
        )


    companion object : KLogging()
}
