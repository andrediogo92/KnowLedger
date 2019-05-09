package pt.um.masb.ledger.storage.transactions

import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.metadata.schema.OSchema
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.masb.common.Hash
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.common.storage.results.DataListResult
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.common.storage.results.QueryResult
import pt.um.masb.common.truncated
import pt.um.masb.ledger.Block
import pt.um.masb.ledger.BlockHeader
import pt.um.masb.ledger.LedgerContract
import pt.um.masb.ledger.Transaction
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.results.*
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.results.LedgerListResult
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.loaders.BlockChainLoaders
import pt.um.masb.ledger.storage.loaders.ChainLoadable
import pt.um.masb.ledger.storage.loaders.DefaultLoadable
import pt.um.masb.ledger.storage.loaders.Loadable
import pt.um.masb.ledger.storage.loaders.QueryLoadable
import pt.um.masb.ledger.storage.query.ClusterSelect
import pt.um.masb.ledger.storage.query.Filters
import pt.um.masb.ledger.storage.query.GenericQuery
import pt.um.masb.ledger.storage.query.SimpleBinaryOperator
import pt.um.masb.ledger.storage.schema.PreConfiguredSchemas
import pt.um.masb.ledger.storage.schema.SchemaProvider
import java.security.PublicKey


/**
 * A Thread-safe wrapper into a DB context
 * for the ledger library.
 */
internal class PersistenceWrapper(
    private val db: ManagedSession
) {

    private val dbSchema = db.session.metadata.schema

    init {
        registerDefaultSchemas()
    }

    private fun registerDefaultSchemas(
    ) {
        PreConfiguredSchemas
            .chainSchemas
            .forEach {
                registerSchema(
                    it
                )
            }
    }

    fun registerSchema(
        schemaProvider: SchemaProvider
    ): PersistenceWrapper =
        apply {
            if (!dbSchema.existsClass(schemaProvider.id)) {
                createSchema(
                    dbSchema,
                    schemaProvider
                )
            } else {
                replaceSchema(
                    dbSchema,
                    schemaProvider
                )
            }
        }

    private fun createSchema(
        schema: OSchema,
        provider: SchemaProvider
    ) {
        val cl = schema.createClass(provider.id)
        provider.properties.forEach {
            cl.createProperty(it.key, it.value)
        }
    }


    private fun replaceSchema(
        schema: OSchema,
        provider: SchemaProvider
    ) {
        val cl = schema.getClass(provider.id)
        val (propsIn, propsNotIn) = cl
            .declaredProperties()
            .partition {
                it.name in provider.properties.keys
            }

        if (propsNotIn.isNotEmpty()) {
            //Drop properties that no longer exist in provider.
            propsNotIn.forEach {
                cl.dropProperty(it.name)
            }
        }

        if (propsIn.size != provider.properties.keys.size) {
            //New properties are those in provider that are not already present.
            provider
                .properties
                .keys
                .asSequence()
                .filter {
                    it !in propsIn.map { elem -> elem.name }
                }.forEach {
                    cl.createProperty(
                        it,
                        provider.properties[it]
                    )
                }
        }
    }

    internal fun registerDefaultClusters(
        blockChainId: Hash
    ): PersistenceWrapper =
        apply {
            val trunc = blockChainId.truncated()
            //For all schemas except Ident add a new chain-specific cluster.
            //For transactions and blocks add temporary pools.
            PreConfiguredSchemas.chainSchemas.forEach {
                dbSchema.getClass(
                    it.id
                ).addCluster(
                    "${it.id}$trunc"
                )
            }.also {
                dbSchema.getClass(
                    "Transaction"
                ).addCluster(
                    "TransactionPool$trunc"
                )
                dbSchema.getClass(
                    "Block"
                ).addCluster(
                    "BlockPool$trunc"
                )
            }
        }

    internal fun closeCurrentSession(): PersistenceWrapper =
        apply {
            db.session.close()
        }

    fun getInstanceSession(): NewInstanceSession =
        NewInstanceSession(db.session)

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
            db.session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                loader.load(it.toElement())
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
        loader: DefaultLoadable<T>
    ): LoadResult<T> =
        tryOrLoadQueryFailure {
            db.session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                (loader.load)(
                    blockChainId,
                    it.toElement()
                )
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
        loader: ChainLoadable<T>
    ): LedgerResult<T> =
        tryOrLedgerQueryFailure {
            db.session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                (loader.load)(
                    crypterHash,
                    this,
                    it.toElement()
                )
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
            db.session.query(
                query.query,
                query.params
            ).asSequence().firstOrNull()?.let {
                (loader.load)(it.toElement())
            } ?: QueryResult.NonExistentData(
                "Empty ResultSet for ${query.query}"
            )
        }


    /**
     * Requires:
     * - The [session] in which to execute the query.
     * - A [query] with the command to execute
     * and it's arguments.
     * - A [Loadable] that converts from documents to
     * a usable user-type that implements [BlockChainData].
     *
     *
     * Returns a [DataListResult] over a list.
     */
    private fun <T : BlockChainData> queryResults(
        session: ODatabaseDocument,
        query: GenericQuery,
        loader: Loadable<T>
    ): DataListResult<T> =
        tryOrDataListQueryFailure {
            session.query(
                query.query,
                query.params
            ).asSequence().map {
                (loader.load)(it.toElement())
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
        loader: DefaultLoadable<T>
    ): LoadListResult<T> =
        tryOrLoadListQueryFailure {
            db.session.query(
                query.query,
                query.params
            ).asSequence().map {
                (loader.load)(blockChainId, it.toElement())
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
        loader: ChainLoadable<T>
    ): LedgerListResult<T> =
        tryOrLedgerListQueryFailure {
            db.session.query(
                query.query,
                query.params
            ).asSequence().map {
                (loader.load)(crypterHash, this, it.toElement())
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
            db.session.query(
                query.query,
                query.params
            ).asSequence().map {
                (loader.load)(it.toElement())
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
     * Returns a [QueryResult] over an [ORID].
     */
    @Synchronized
    internal fun persistEntity(
        element: Storable
    ): QueryResult<ORID> =
        tryOrQueryQueryFailure {
            val elem = element.store(NewInstanceSession(db.session))
            val r = db.session.save<OElement>(elem)
            if (r != null) {
                QueryResult.Success(r.identity)
            } else {
                QueryResult.NonExistentData<ORID>(
                    "Failed to save element ${elem.toJSON()}"
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
            BlockChainLoaders.ledgerLoader
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
            BlockChainLoaders.ledgerLoader
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
            BlockChainLoaders.blockHeaderLoader
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
            BlockChainLoaders.blockHeaderLoader
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
            BlockChainLoaders.blockHeaderLoader
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
            BlockChainLoaders.blockHeaderLoader
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
            BlockChainLoaders.blockLoader
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
            BlockChainLoaders.blockLoader
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
            BlockChainLoaders.blockLoader
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
            BlockChainLoaders.blockLoader
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
            BlockChainLoaders.blockLoader
        )


    // ------------------------------
    // Ident transaction.
    //
    // ------------------------------


    internal fun getIdent(id: String): OElement? =
        db.session.query(
            "SELECT * FROM ident WHERE id = :id",
            id
        ).let {
            if (it.hasNext()) {
                it.next().toElement()
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
            BlockChainLoaders.transactionLoader
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
            BlockChainLoaders.transactionLoader
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
            BlockChainLoaders.transactionLoader
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
            BlockChainLoaders.transactionLoader
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
            BlockChainLoaders.chainLoader
        )

    fun tryAddChainHandle(
        chainHandle: ChainHandle
    ): QueryResult<ORID> =
        persistEntity(chainHandle)

    fun getKnownChainHandleTypes(
        ledgerHash: Hash
    ): QueryResult<List<String>> =
        queryResults<String>(
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ).withProjection(
                "clazz"
            ),
            QueryLoadable {
                QueryResult.Success(it.getProperty<String>("clazz"))
            }
        )

    fun getKnownChainHandleIDs(
        ledgerHash: Hash
    ): QueryResult<List<ORID>> =
        queryResults<ORID>(
            ClusterSelect(
                "ChainHandle",
                ledgerHash
            ),
            QueryLoadable {
                QueryResult.Success(it.identity)
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
            BlockChainLoaders.chainLoader
        )


    companion object : KLogging()
}
