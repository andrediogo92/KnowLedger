package org.knowledger.ledger.service.handles

import kotlinx.serialization.BinaryFormat
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.GlobalLedgerConfiguration
import org.knowledger.ledger.config.GlobalLedgerConfiguration.CACHE_SIZE
import org.knowledger.ledger.config.GlobalLedgerConfiguration.RECALC_DIV
import org.knowledger.ledger.config.GlobalLedgerConfiguration.RECALC_MULT
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.core.base.data.Difficulty.Companion.INIT_DIFFICULTY
import org.knowledger.ledger.core.base.data.Difficulty.Companion.MAX_DIFFICULTY
import org.knowledger.ledger.core.base.data.Difficulty.Companion.MIN_DIFFICULTY
import org.knowledger.ledger.core.base.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.mining.BlockState
import org.knowledger.ledger.results.*
import org.knowledger.ledger.service.ChainInfo
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.pools.block.BlockPool
import org.knowledger.ledger.service.pools.block.BlockPoolImpl
import org.knowledger.ledger.service.pools.transaction.StorageAwareTransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.transactions.*
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.BlockUpdates
import org.knowledger.ledger.storage.block.StorageAwareBlock
import org.knowledger.ledger.storage.block.TransactionAdding
import org.knowledger.ledger.storage.block.WitnessCalculator
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.convertToStorageAware
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.tinylog.kotlin.Logger
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * A facade into useful methods for managing a
 * unique chain in the ledger represented by the [id].
 */
class ChainHandle private constructor(
    val ledgerInfo: LedgerInfo,
    internal val adapterManager: AdapterManager,
    val id: ChainId,
    val chainInfo: ChainInfo = ChainInfo(),
    internal val transactionPool: TransactionPool =
        StorageAwareTransactionPool(adapterManager, id)
) : ServiceClass {
    val chainHash = id.hash
    private lateinit var queryManager: QueryManager
    private val hasher: Hashers = ledgerInfo.hasher
    private val encoder: BinaryFormat = ledgerInfo.encoder
    private val blockPool: BlockPool = BlockPoolImpl(id, ledgerInfo, chainInfo)
    val ledgerParams: LedgerParams = ledgerInfo.ledgerParams
    val blockParams: BlockParams = ledgerParams.blockParams
    val coinbaseParams: CoinbaseParams = ledgerInfo.coinbaseParams

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
        get() = queryManager.getLatestBlock(chainHash)


    /**
     * Returns a [LoadFailure] for the tail-end
     * [BlockHeader] in the ledger.
     */
    val lastBlockHeader: Outcome<BlockHeader, LoadFailure>
        get() = queryManager.getLatestBlockHeader(chainHash)


    internal constructor(
        ledgerInfo: LedgerInfo,
        adapterManager: AdapterManager,
        tag: Tag
    ) : this(
        ledgerInfo,
        adapterManager,
        StorageAwareChainId(
            tag, ledgerInfo.ledgerId.hash,
            ledgerInfo.hasher, ledgerInfo.encoder
        )
    )


    internal constructor(
        container: LedgerInfo,
        adapterManager: AdapterManager,
        id: ChainId,
        transactionPool: TransactionPool,
        difficulty: Difficulty,
        lastRecalc: Int,
        currentBlockheight: Long
    ) : this(
        container, adapterManager, id,
        ChainInfo(difficulty, lastRecalc, currentBlockheight),
        transactionPool
    )

    internal fun addQueryManager(queryManager: QueryManager) {
        this.queryManager = queryManager
    }

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
            queryManager.getBlockByBlockHeight(chainHash, 1)
        lateinit var previousLastBlock: Block
        var failure: Outcome<Boolean, QueryFailure>
        failure = when (blockResult) {
            is Outcome.Error -> {
                valid = false
                Outcome.Error(blockResult.failure.intoQuery())
            }
            is Outcome.Ok -> {
                previousLastBlock = blockResult.value
                Outcome.Ok(true)
            }
        }
        var lowIndex = -cacheSize + 2L
        var highIndex = 2L
        while (highIndex - cacheSize <= currentBlockheight && valid) {
            lowIndex += cacheSize
            highIndex += cacheSize
            val blocks =
                queryManager.getBlockListByBlockHeightInterval(
                    chainHash,
                    lowIndex,
                    highIndex
                )
            when (blocks) {
                is Outcome.Ok -> {
                    valid = checkBlocks(
                        previousLastBlock, blocks.value.iterator()
                    )
                }
                is Outcome.Error -> {
                    failure = Outcome.Error(blocks.failure.intoQuery())
                    valid = false
                }
            }
        }
        return failure
    }

    private fun checkBlocks(
        lastBlock: Block, data: Iterator<Block>
    ): Boolean {
        // loop through ledger to check hashes:
        var previousBlock = lastBlock
        while (data.hasNext()) {
            val currentBlock = data.next()
            val curHeader = currentBlock.header
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
            val prevHeader = previousBlock.header
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

            val hashTarget = currentBlock.coinbase.difficulty
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
        queryManager.getBlockByHeaderHash(
            chainHash, hash
        )

    /**
     * Takes a [blockheight] and returns a [LoadFailure]
     * over a [Block] with the provided [blockheight].
     */
    fun getBlockByHeight(
        blockheight: Long
    ): Outcome<Block, LoadFailure> =
        queryManager.getBlockByBlockHeight(
            chainHash, blockheight
        )

    /**
     * Takes the [hash] of a block and returns a [LoadFailure]
     * over a [BlockHeader] with the provided [hash].
     */
    fun getBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        queryManager.getBlockHeaderByHash(
            chainHash, hash
        )

    /**
     * Returns whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        (queryManager.getBlockByHeaderHash(
            chainHash, hash
        ) is Outcome.Ok<*>)

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [Block] previous to that which has [hash].
     */
    fun getPrevBlock(
        hash: Hash
    ): Outcome<Block, LoadFailure> =
        queryManager.getBlockByPrevHeaderHash(
            chainHash, hash
        )

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [BlockHeader] previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        queryManager.getBlockHeaderByPrevHeaderHash(
            chainHash, hash
        )


    /**
     * Returns an [Outcome] over the requested [Sequence] of [Block]
     * for the specified blockheight [range].
     */
    fun getBlockChunk(
        range: LongRange
    ): Outcome<Sequence<Block>, LoadFailure> =
        getBlockChunk(range.first, range.last)


    /**
     * Preferrable overload, returns an [Outcome] over
     * [Block]s for the specified blockheight interval, from
     * [startInclusive] to [endInclusive].
     */
    fun getBlockChunk(
        startInclusive: Long, endInclusive: Long
    ): Outcome<Sequence<Block>, LoadFailure> =
        queryManager.getBlockListByBlockHeightInterval(
            chainHash,
            startInclusive,
            endInclusive
        )

    fun getBlockChunk(
        start: Hash, chunkSize: Long
    ): Outcome<Sequence<Block>, LoadFailure> =
        queryManager.getBlockListByHash(
            chainHash,
            start,
            chunkSize
        )


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
    fun addBlock(block: Block): Outcome<Boolean, QueryFailure> =
        lastBlockHeader.flatMapSuccess {
            val recalcTrigger = ledgerParams.recalculationTrigger
            if (validateBlock(it.hash, block)) {
                if (lastRecalculation == recalcTrigger) {
                    recalculateDifficulty(block)
                    chainInfo.resetRecalculation()
                } else {
                    chainInfo.incrementRecalculation()
                }
                Outcome.Ok(true)
            } else {
                Outcome.Ok(false)
            }
        }.fold(
            {
                Outcome.Error(
                    it.intoQuery()
                )
            },
            {
                if (it) {
                    queryManager.persistEntity(
                        block,
                        adapterManager.blockStorageAdapter
                    ).mapSuccess {
                        true
                    }
                } else {
                    Outcome.Ok(it)
                }
            }
        )


    /**
     * Will check if the block beats the current difficulty,
     * the current difficulty is correct, and transactions are
     * also valid.
     *
     * Returns whether the [block] is valid in regards to
     * the [hash] provided.
     */
    private fun validateBlock(
        hash: Hash,
        block: Block
    ): Boolean {
        if (block.header.previousHash == hash) {
            if (block.header.hash.toDifficulty() <=
                block.coinbase.difficulty
            ) {
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
    private fun recalculateDifficulty(
        triggerBlock: Block
    ): Difficulty {
        val cmp = triggerBlock.coinbase.blockheight
        val cstamp = triggerBlock.header.seconds
        val fromHeight = cmp - ledgerParams.recalculationTrigger
        val recalcBlock =
            queryManager.getBlockByBlockHeight(chainHash, fromHeight)
        return when (recalcBlock) {
            is Outcome.Ok<Block> -> {
                val pstamp = recalcBlock.value.header.seconds
                val deltaStamp = cstamp - pstamp
                recalc(triggerBlock, recalcBlock.value, deltaStamp)
            }
            is Outcome.Error<LoadFailure> -> {
                Logger.error {
                    """
                    | Difficulty retrigger without 2048 blocks existent?
                    |   Grab from Index: $fromHeight
                    |   Cause: ${recalcBlock.failure.failable.cause}
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
    private fun recalc(
        triggerBlock: Block,
        recalcBlock: Block,
        deltaStamp: Long
    ): Difficulty {
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
    private fun padOrMax(calcDiff: Difficulty): Difficulty {
        return when {
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
    }

    /**
     * Add a transaction to the transaction pool.
     */
    fun addTransaction(
        t: Transaction
    ): BlockState {
        val internal = t.convertToStorageAware {
            StorageAwareTransaction(this as HashedTransactionImpl)
        }
        if (internal.processTransaction(encoder)) {
            transactionPool += internal
            val block = blockPool.firstUnconfirmedNotFull ?: newBlock().also { block ->
                blockPool += block
            }
            (block as TransactionAdding) + internal
            calculateTransactionDifference(block, internal)
            return if (block.miningReady) {
                BlockState.BlockReady(block.full, block.header)
            } else {
                BlockState.BlockNotReady
            }
        }
        return BlockState.BlockFailure
    }

    private fun calculateTransactionDifference(block: Block, newTransaction: Transaction) {
        val previousTransaction = block.transactions
            .filter { it == newTransaction }
            .minBy {
                abs(it.data.millis - newTransaction.data.millis)
            }
        val diff = if (previousTransaction != null) {
            abs(newTransaction.data.millis - previousTransaction.data.millis)
        } else {
            abs(newTransaction.data.millis - 1)
        }
        val reference: Outcome<TransactionWithBlockHash, LoadFailure> =
            queryManager.getTransactionByBound(newTransaction.data.millis, diff)
        val witnessIndex = block.coinbase.findWitness(newTransaction)

        reference.mapSuccess { withBlockHash ->
            (block as WitnessCalculator).calculateWitnessWithExistingTransaction(
                witnessIndex, withBlockHash, newTransaction
            )
        }.peekFailure { fail ->
            when (fail) {
                is LoadFailure.NonExistentData ->
                    (block as WitnessCalculator).calculateWitnessWithNoExistingTransaction(
                        witnessIndex, newTransaction
                    )
                else -> throw RuntimeException(fail.failable.cause)
            }
        }
    }

    private fun WitnessCalculator.calculateWitnessWithNoExistingTransaction(
        witnessIndex: Int, newTransaction: Transaction
    ) {
        if (witnessIndex > 0) {
            calculateWitness(
                newTransaction = newTransaction,
                witnessIndex = witnessIndex
            )
        } else {
            val TXOStorageAdapter: TransactionOutputStorageAdapter =
                queryManager.transactionOutputStorageAdapter
            queryManager
                .getWitnessInfoBy(newTransaction.publicKey)
                .mapSuccess { info ->
                    calculateWitness(
                        newTransaction = newTransaction,
                        transactionOutputStorageAdapter = TXOStorageAdapter,
                        previousWitnessIndex = info.index,
                        coinbaseHash = info.hash
                    )
                }.peekFailure { fail ->
                    when (fail) {
                        is LoadFailure.NonExistentData ->
                            calculateWitness(
                                newTransaction = newTransaction,
                                transactionOutputStorageAdapter = TXOStorageAdapter
                            )
                        else -> fail.unwrap()
                    }
                }
        }
    }

    private fun WitnessCalculator.calculateWitnessWithExistingTransaction(
        witnessIndex: Int,
        withBlockHash: TransactionWithBlockHash,
        newTransaction: Transaction
    ) {
        if (witnessIndex > 0) {
            calculateWitness(
                witnessIndex = witnessIndex,
                newTransaction = newTransaction,
                lastTransaction = withBlockHash
            )
        } else {
            val TXOStorageAdapter: TransactionOutputStorageAdapter =
                queryManager.transactionOutputStorageAdapter
            queryManager
                .getWitnessInfoBy(newTransaction.publicKey)
                .mapSuccess { info ->
                    calculateWitness(
                        newTransaction = newTransaction,
                        transactionOutputStorageAdapter = TXOStorageAdapter,
                        lastTransaction = withBlockHash,
                        previousWitnessIndex = info.index,
                        coinbaseHash = info.hash
                    )
                }.peekFailure { fail ->
                    when (fail) {
                        is LoadFailure.NonExistentData ->
                            calculateWitness(
                                newTransaction = newTransaction,
                                transactionOutputStorageAdapter = TXOStorageAdapter,
                                lastTransaction = withBlockHash
                            )
                        else -> throw RuntimeException(fail.failable.cause)
                    }
                }
        }
    }

    private fun newBlock(): Block {
        val previousHash: Hash = blockPool.current?.header?.hash
            ?: lastBlock.reduce<Hash, Block, LoadFailure>(
                { block ->
                    block.header.hash
                }, {
                    originHeader(ledgerInfo, id).hash
                }
            )
        return StorageAwareBlock(
            adapterManager = adapterManager,
            ledgerInfo = ledgerInfo,
            chainId = id,
            previousHash = previousHash,
            blockParams = ledgerParams.blockParams
        )
    }

    fun checkAgainstTarget(hashId: Hash): Boolean =
        hashId.toDifficulty() <= currentDifficulty

    fun refreshHeader(merkleRoot: Hash): BlockState =
        blockPool[merkleRoot]?.let {
            (it as BlockUpdates).newExtraNonce()
            BlockState.BlockReady(it.full, it.header)
        } ?: BlockState.BlockFailure


    companion object {
        val identity = Identity("")

        fun getOriginHeader(
            ledgerInfo: LedgerInfo,
            chainId: ChainId
        ): BlockHeader =
            originHeader(ledgerInfo, chainId)

        private fun originHeader(
            ledgerInfo: LedgerInfo,
            chainId: ChainId
        ): BlockHeader =
            HashedBlockHeaderImpl(
                chainId,
                ledgerInfo.hasher,
                ledgerInfo.encoder,
                emptyHash,
                BlockParams(),
                emptyHash,
                emptyHash,
                ZonedDateTime.of(
                    2018, 3, 13, 0,
                    0, 0, 0, ZoneOffset.UTC
                ).toEpochSecond(),
                0L
            )


        fun getOriginBlock(
            ledgerInfo: LedgerInfo,
            chainId: ChainId
        ): Block =
            BlockImpl(
                mutableSortedListOf(),
                HashedCoinbaseImpl(
                    ledgerInfo
                ),
                originHeader(ledgerInfo, chainId),
                MerkleTreeImpl(hasher = ledgerInfo.hasher)
            ).also {
                it.markForMining(0, INIT_DIFFICULTY)
            }
    }

}

