package org.knowledger.ledger.chain.handles

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import kotlinx.datetime.DateTimePeriod
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.builders.ChainBuilder
import org.knowledger.ledger.builders.WorkingChainBuilder
import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.LedgerInfo
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.ServiceClass
import org.knowledger.ledger.chain.service.Services
import org.knowledger.ledger.chain.transactions.QueryManager
import org.knowledger.ledger.chain.transactions.getBlockByBlockHeight
import org.knowledger.ledger.chain.transactions.getBlockByHeaderHash
import org.knowledger.ledger.chain.transactions.getBlockByPrevHeaderHash
import org.knowledger.ledger.chain.transactions.getBlockHeaderByHash
import org.knowledger.ledger.chain.transactions.getBlockHeaderByPrevHeaderHash
import org.knowledger.ledger.chain.transactions.getBlockListByBlockHeightInterval
import org.knowledger.ledger.chain.transactions.getBlockListByHash
import org.knowledger.ledger.chain.transactions.getLatestBlock
import org.knowledger.ledger.chain.transactions.getLatestBlockHeader
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.data.Difficulty.Companion.INIT_DIFFICULTY
import org.knowledger.ledger.core.data.Difficulty.Companion.MAX_DIFFICULTY
import org.knowledger.ledger.core.data.Difficulty.Companion.MIN_DIFFICULTY
import org.knowledger.ledger.core.data.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.toDifficulty
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.mining.BlockState
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableBlockPool
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration.CACHE_SIZE
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration.RECALC_DIV
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration.RECALC_MULT
import org.knowledger.ledger.storage.config.chainid.ChainId
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.intoQuery
import org.tinylog.kotlin.Logger
import java.math.BigDecimal
import java.math.BigInteger

/**
 * A facade into useful methods for managing a
 * unique chain in the ledger represented by the [chainId].
 */
@OptIn(ExperimentalSerializationApi::class)
class ChainHandle private constructor(
    val chainId: ChainId,
    val ledgerInfo: LedgerInfo,
    internal val context: PersistenceContext,
    val chainInfo: ChainInfo = ChainInfo(
        ledgerInfo.hashers, ledgerInfo.encoder, chainId.blockParams,
        chainId.coinbaseParams, ledgerInfo.factories, ledgerInfo.formula
    ),
    internal val transactionPool: MutableTransactionPool =
        context.transactionPoolFactory.create(chainId, mutableSortedListOf()),
    internal val blockPool: MutableBlockPool = context.blockPoolFactory.create(chainId),
) : ServiceClass {
    private val services: Services = Services()
    private lateinit var queryManager: QueryManager
    private val hasher: Hashers = chainInfo.hashers
    private val encoder: BinaryFormat = chainInfo.encoder
    val ledgerParams: LedgerParams = ledgerInfo.ledgerId.ledgerParams
    val blockParams: BlockParams = chainInfo.blockParams
    val coinbaseParams: CoinbaseParams = chainInfo.coinbaseParams

    val currentDifficulty
        get() = chainInfo.currentDifficulty

    val lastRecalculation
        get() = chainInfo.lastRecalculation

    val currentBlockheight
        get() = chainInfo.currentBlockheight

    /**
     * Returns a [LoadFailure] for the tail-end
     * [Block] in the ledger.
     */
    val lastBlock: Outcome<Block, LoadFailure>
        get() = _lastBlock

    private val _lastBlock: Outcome<MutableBlock, LoadFailure>
        get() = queryManager.getLatestBlock()

    /**
     * Returns a [LoadFailure] for the tail-end
     * [BlockHeader] in the ledger.
     */
    val lastBlockHeader: Outcome<BlockHeader, LoadFailure>
        get() = queryManager.getLatestBlockHeader()


    internal constructor(
        ledgerInfo: LedgerInfo, context: PersistenceContext,
        tag: Tag, rawTag: Hash, blockParams: BlockParams,
        coinbaseParams: CoinbaseParams,
    ) : this(
        context.chainIdFactory.create(
            ledgerInfo.ledgerId.hash, tag, rawTag, ledgerInfo.hashers,
            ledgerInfo.encoder, blockParams, coinbaseParams
        ), ledgerInfo, context
    )


    internal constructor(
        ledgerInfo: LedgerInfo, context: PersistenceContext, chainId: ChainId,
        transactionPool: MutableTransactionPool, blockPool: MutableBlockPool,
        difficulty: Difficulty, lastRecalc: Int, currentBlockheight: Long,
    ) : this(
        chainId, ledgerInfo, context, ChainInfo(
            ledgerInfo.hashers, ledgerInfo.encoder, chainId.blockParams,
            chainId.coinbaseParams, ledgerInfo.factories, ledgerInfo.formula,
            difficulty, lastRecalc, currentBlockheight
        ), transactionPool, blockPool
    )

    internal fun addQueryManager(queryManager: QueryManager) {
        this.queryManager = queryManager
    }

    fun chainBuilder(identity: Identity): ChainBuilder =
        WorkingChainBuilder(context, ledgerInfo, chainId, identity)

    /**
     * Checks integrity of the entire cached ledger.
     *
     * TODO: actually check entire ledger in
     * ranges of [GlobalLedgerConfiguration.CACHE_SIZE] blocks.
     *
     * Returns whether the chain is valid.
     */
    fun isChainValid(): Outcome<Boolean, QueryFailure> {
        val cacheSize = CACHE_SIZE
        var valid = true
        val blockResult =
            queryManager.getBlockByBlockHeight(1)
        lateinit var previousLastBlock: Block
        var failure: Outcome<Boolean, QueryFailure>
        failure = when (blockResult) {
            is Ok -> {
                previousLastBlock = blockResult.value
                true.ok()
            }
            is Err -> {
                valid = false
                blockResult.error.intoQuery().err()
            }
        }
        var lowIndex = -cacheSize + 2L
        var highIndex = 2L
        while (highIndex - cacheSize <= currentBlockheight && valid) {
            lowIndex += cacheSize
            highIndex += cacheSize
            val blocks =
                queryManager.getBlockListByBlockHeightInterval(lowIndex, highIndex)
            when (blocks) {
                is Ok -> {
                    valid = checkBlocks(previousLastBlock, blocks.value.iterator())
                }
                is Err -> {
                    valid = false
                    failure = blocks.error.intoQuery().err()
                }
            }
        }
        return failure
    }

    private fun checkBlocks(
        lastBlock: Block, data: Iterator<Block>,
    ): Boolean {
        // loop through ledger to check hashes:
        var previousBlock = lastBlock
        while (data.hasNext()) {
            val currentBlock = data.next()
            val curHeader = currentBlock.blockHeader
            val cmpHash = curHeader.digest(hasher, encoder)
            // compare registered hashId and calculated hashId:
            if (curHeader.hash != cmpHash) {
                Logger.debug {
                    """
                    |Current Hashes not equal:
                    |   ${curHeader.hash.base64Encoded()}
                    |   -- and --
                    |   ${cmpHash.base64Encoded()}
                    """.trimMargin()
                }
                return false
            }
            val prevHeader = previousBlock.blockHeader
            // compare previous hashId and registered previous hashId
            if (prevHeader.hash != curHeader.previousHash) {
                Logger.debug {
                    """
                    |Previous Hashes not equal:
                    |   ${prevHeader.hash.base64Encoded()}
                    |   -- and --
                    |   ${curHeader.previousHash.base64Encoded()}
                    """.trimMargin()
                }
                return false
            }

            val hashTarget = currentBlock.coinbase.coinbaseHeader.difficulty
            val curDiff = curHeader.hash.toDifficulty()
            if (curDiff > hashTarget) {
                Logger.debug {
                    "Unmined block: ${curHeader.hash.base64Encoded()}"
                }
                return false
            }

            previousBlock = currentBlock
        }
        return true
    }

    /**
     * Takes the [hash] of a block and returns a [LoadFailure]
     * over a [Block] with the provided [hash].
     */
    fun getBlock(hash: Hash): Outcome<Block, LoadFailure> =
        queryManager.getBlockByHeaderHash(hash)

    /**
     * Takes a [blockheight] and returns a [LoadFailure]
     * over a [Block] with the provided [blockheight].
     */
    fun getBlockByHeight(blockheight: Long): Outcome<Block, LoadFailure> =
        queryManager.getBlockByBlockHeight(blockheight)

    /**
     * Takes the [hash] of a block and returns a [LoadFailure]
     * over a [BlockHeader] with the provided [hash].
     */
    fun getBlockHeaderByHash(hash: Hash): Outcome<BlockHeader, LoadFailure> =
        queryManager.getBlockHeaderByHash(hash)

    /**
     * Returns whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        (queryManager.getBlockByHeaderHash(hash) is Ok<*>)

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [Block] previous to that which has [hash].
     */
    fun getPrevBlock(hash: Hash): Outcome<Block, LoadFailure> =
        queryManager.getBlockByPrevHeaderHash(hash)

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [BlockHeader] previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(hash: Hash): Outcome<BlockHeader, LoadFailure> =
        queryManager.getBlockHeaderByPrevHeaderHash(hash)


    /**
     * Returns an [Outcome] over the requested [Sequence] of [Block]
     * for the specified blockheight [range].
     */
    fun getBlockChunk(range: LongRange): Outcome<List<Block>, LoadFailure> =
        getBlockChunk(range.first, range.last)


    /**
     * Preferrable overload, returns an [Outcome] over
     * [Block]s for the specified blockheight interval, from
     * [startInclusive] to [endInclusive].
     */
    fun getBlockChunk(startInclusive: Long, endInclusive: Long): Outcome<List<Block>, LoadFailure> =
        queryManager.getBlockListByBlockHeightInterval(startInclusive, endInclusive)

    fun getBlockChunk(start: Hash, chunkSize: Long): Outcome<List<Block>, LoadFailure> =
        queryManager.getBlockListByHash(start, chunkSize)


    /**
     * Attempts to add the [block] to the ledger if block is valid.
     *
     * May trigger difficulty recalculation.
     *
     *
     * TODO: Verify coinbase and invalidate mempool.
     *
     * Returns whether the block was successfully added.
     */
    fun addBlock(block: ImmutableBlock): Outcome<Boolean, QueryFailure> =
        lastBlockHeader.map {
            val recalcTrigger = ledgerParams.recalculationTrigger
            if (validateBlock(it.hash, block)) {
                if (lastRecalculation == recalcTrigger) {
                    recalculateDifficulty(block)
                    chainInfo.resetRecalculation()
                } else {
                    chainInfo.incrementRecalculation()
                }
                return@map true
            }
            return@map false
        }.mapError(LoadFailure::intoQuery).andThen { blockAddable ->
            val mutBlock = context.factories.blockFactory.create(block)
            if (blockAddable) {
                queryManager.persistEntity(
                    mutBlock, context.blockStorageAdapter
                ).map { true }
            } else {
                blockAddable.ok()
            }
        }


    /**
     * Will check if the block beats the current difficulty,
     * the current difficulty is correct, and transactions are
     * also valid.
     *
     * Returns whether the [block] is valid in regards to
     * the [hash] provided.
     */
    private fun validateBlock(hash: Hash, block: Block): Boolean {
        if (block.blockHeader.previousHash == hash) {
            if (block.blockHeader.hash.toDifficulty() <= block.coinbase.coinbaseHeader.difficulty) {
                return block.verifyTransactions()
            }
        }
        return false
    }

    /**
     * Difficulty is recalculated based on timestamp
     * difference between [triggerBlock] at current blockheight
     * and Block at current blockheight - [ledgerParams]'s recalcTrigger.
     *
     * This difference is measured as a percentage of
     * [ledgerParams]'s recalcTime which is used to multiply by current
     * difficulty target.
     *
     * Returns the recalculated difficulty or the same
     * difficulty if re-triggered erroneously.
     */
    private fun recalculateDifficulty(triggerBlock: Block): Difficulty {
        val cmp = triggerBlock.coinbase.coinbaseHeader.blockheight
        val cstamp = triggerBlock.blockHeader.seconds
        val fromHeight = cmp - ledgerParams.recalculationTrigger
        val recalcBlock = queryManager.getBlockByBlockHeight(fromHeight)
        return when (recalcBlock) {
            is Ok<Block> -> {
                val pstamp = recalcBlock.value.blockHeader.seconds
                val deltaStamp = cstamp - pstamp
                recalc(triggerBlock, recalcBlock.value, deltaStamp)
            }
            is Err<LoadFailure> -> {
                Logger.error {
                    """
                    | Difficulty retrigger without 2048 blocks existent?
                    |   Grab from Index: $fromHeight
                    |   Cause: ${recalcBlock.error.failable.cause}
                    """.trimMargin()
                }
                currentDifficulty
            }
        }
    }

    /**
     * Actual recalculation logic.
     *
     * Only uses the positive possible integer values.
     *
     * Use BigDecimal to calculate an approximate multiplier
     * which is massively inflated in order to cover sufficient
     * decimal points in division before conversion to BigInteger.
     *
     * It's then massively deflated back to preserve original scale.
     */
    private fun recalc(triggerBlock: Block, recalcBlock: Block, deltaStamp: Long): Difficulty {
        val recalcMult = RECALC_MULT
        val recalcDiv = RECALC_DIV
        val deltax = BigDecimal(ledgerParams.recalculationTime - deltaStamp)
        val deltadiv = (deltax * recalcMult)
            .divideToIntegralValue(BigDecimal(ledgerParams.recalculationTime))
            .toBigInteger()
        val difficulty = BigInteger(currentDifficulty.bytes)
        val newDiff = difficulty + (difficulty * deltadiv)
        return padOrMax(Difficulty(newDiff / recalcDiv))
    }

    /**
     * Check for min and max difficulty bounds.
     */
    private fun padOrMax(calcDiff: Difficulty): Difficulty =
        when {
            calcDiff < MAX_DIFFICULTY -> calcDiff
            calcDiff < MIN_DIFFICULTY -> MIN_DIFFICULTY
            calcDiff > MAX_DIFFICULTY -> MAX_DIFFICULTY
            else -> {
                Logger.error {
                    "Difficulty not within expected bounds: $calcDiff"
                }
                calcDiff
            }
        }

    /**
     * Add a transaction to the transaction pool.
     */
    fun addTransaction(transaction: Transaction): BlockState {
        val tx = context.transactionFactory.create(transaction)
        if (tx.processTransaction(encoder)) {
            transactionPool += tx
            val block = blockPool.firstUnconfirmedNotFull ?: newBlock().also { block ->
                blockPool += block
            }
            services.transactionService.calculateTransactionDifference(
                block, tx, queryManager, chainInfo, services.witnessService
            )
            return if (block.miningReady) {
                BlockState.BlockReady(block.full, block.blockHeader)
            } else {
                BlockState.BlockNotReady
            }
        }
        return BlockState.BlockFailure
    }


    private fun newBlock(): MutableBlock {
        val previousHash: Hash = blockPool.current?.blockHeader?.hash
                                 ?: _lastBlock.map { it.blockHeader.hash }
                                     .getOrElse { originHeader(context, chainId).hash }
        return context.blockFactory.create(
            chainId.hash, previousHash, blockParams, coinbaseParams, hasher, encoder
        )
    }

    fun checkAgainstTarget(hashId: Hash): Boolean =
        hashId.toDifficulty() <= currentDifficulty

    fun refreshHeader(merkleRoot: Hash): BlockState =
        blockPool[merkleRoot]?.let { block ->
            block.newExtraNonce()
            BlockState.BlockReady(block.full, block.blockHeader)
        } ?: BlockState.BlockFailure


    companion object {
        private val identity = Identity("")

        internal fun getOriginHeader(context: PersistenceContext, chainId: ChainId): BlockHeader =
            originHeader(context, chainId)

        private fun originHeader(
            context: PersistenceContext, chainId: ChainId,
        ): MutableBlockHeader =
            context.blockHeaderFactory.create(
                chainId.hash, emptyHash, emptyHash, context.blockParamsFactory.create(),
                emptyHash, DateTimePeriod(2018, 3, 13, 0, 0, 0, 0).seconds
            )


        internal fun getOriginBlock(
            context: PersistenceContext, ledgerInfo: LedgerInfo, chainId: ChainId,
        ): Block {
            val coinbase = context.coinbaseFactory.create(
                chainId.coinbaseParams, ledgerInfo.hashers, ledgerInfo.encoder
            )
            val merkleTree = context.merkleTreeFactory.create(ledgerInfo.hashers)
            val block = context.blockFactory.create(
                originHeader(context, chainId), coinbase, merkleTree, mutableSortedListOf()
            )
            block.markForMining(0, INIT_DIFFICULTY)
            return block
        }
    }

}

