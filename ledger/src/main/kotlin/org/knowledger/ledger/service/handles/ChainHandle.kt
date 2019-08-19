package org.knowledger.ledger.service.handles

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Difficulty.Companion.INIT_DIFFICULTY
import org.knowledger.ledger.core.data.Difficulty.Companion.MAX_DIFFICULTY
import org.knowledger.ledger.core.data.Difficulty.Companion.MIN_DIFFICULTY
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.flatMapSuccess
import org.knowledger.ledger.core.results.fold
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.results.intoQuery
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.pool.StorageAwareTransactionPool
import org.knowledger.ledger.service.pool.TransactionPool
import org.knowledger.ledger.service.results.BlockFailure
import org.knowledger.ledger.service.results.BlockState
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.transactions.*
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.block.StorageUnawareBlock
import org.knowledger.ledger.storage.blockheader.StorageUnawareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageUnawareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree
import org.tinylog.kotlin.Logger
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * A facade into useful methods for managing a
 * unique chain in the ledger represented by the [id].
 */
data class ChainHandle internal constructor(
    val id: ChainId,
    val transactionPool: TransactionPool = StorageAwareTransactionPool(id)
) : ServiceClass {
    val chainHash = id.hashId
    private val hasher: Hasher
    private val pw: PersistenceWrapper
    val ledgerParams: LedgerParams
    val coinbaseParams: CoinbaseParams


    init {
        LedgerHandle.getContainer(
            id.ledgerHash
        )!!.let {
            hasher = it.hasher
            pw = it.persistenceWrapper
            ledgerParams = it.ledgerParams
            coinbaseParams = it.coinbaseParams
        }
    }


    private var difficultyTarget =
        INIT_DIFFICULTY

    val currentDifficulty
        get() = difficultyTarget

    private var lastRecalc = 0L

    val lastRecalculation
        get() = lastRecalc

    //Blockheight 1 is Origin which is immediately added.
    private var blockheight = 1L

    val currentBlockheight
        get() = blockheight

    /**
     * Returns a [LoadFailure] for the tail-end
     * [Block] in the ledger.
     */
    val lastBlock: Outcome<Block, LoadFailure>
        get() = pw.getLatestBlock(chainHash)


    /**
     * Returns a [LoadFailure] for the tail-end
     * [BlockHeader] in the ledger.
     */
    val lastBlockHeader: Outcome<BlockHeader, LoadFailure>
        get() = pw.getLatestBlockHeader(chainHash)


    internal constructor(
        tag: Tag,
        ledgerHash: Hash,
        hasher: Hasher
    ) : this(
        StorageAwareChainId(tag, ledgerHash, hasher)
    )


    internal constructor(
        id: ChainId,
        transactionPool: TransactionPool,
        difficulty: Difficulty,
        lastRecalc: Long,
        currentBlockheight: Long
    ) : this(id, transactionPool) {
        this.difficultyTarget = difficulty
        this.lastRecalc = lastRecalc
        this.blockheight = currentBlockheight
    }


    /**
    fun attemptMineBlock(
    invalidate: Boolean,
    time: Boolean
    ): Boolean {
    if (invalidate && time) {
    merkleTree = MerkleTree.buildMerkleTree(
    coinbase,
    value
    )
    header.merkleRoot = merkleTree.root
    header.timestamp = ZonedDateTime
    .now(ZoneOffset.UTC)
    .toInstant()
    header.nonce = 0
    } else if (invalidate) {
    merkleTree = MerkleTree.buildMerkleTree(
    coinbase,
    value
    )
    header.merkleRoot = merkleTree.root
    header.nonce = 0
    } else if (time) {
    header.timestamp = ZonedDateTime
    .now(ZoneOffset.UTC)
    .toInstant()
    header.nonce = 0
    }
    header.updateHash()
    val curDiff = header.hashId.toDifficulty()
    return if (curDiff < header.difficulty) {
    logger.info {
    "Block Mined!!! : ${header.hashId}"
    }
    logger.info {
    "Block contains: ${toString()}"
    }
    true
    } else {
    header.nonce++
    false
    }
    }
     */


    /**
     * Checks integrity of the entire cached ledger.
     *
     * TODO: actually check entire ledger in
     * ranges of [LedgerConfiguration.CACHE_SIZE] blocks.
     *
     * Returns whether the chain is valid.
     */
    fun isChainValid(): Outcome<Boolean, QueryFailure> {
        val cacheSize = LedgerConfiguration.CACHE_SIZE
        var valid = true
        val blockResult =
            pw.getBlockByBlockHeight(chainHash, 1)
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
        var lowIndex = -cacheSize + 2
        var highIndex = 2L
        while (highIndex - cacheSize <= blockheight && valid) {
            lowIndex += cacheSize
            highIndex += cacheSize
            val blocks =
                pw.getBlockListByBlockHeightInterval(
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
            val cmpHash = curHeader.digest(hasher)
            // compare registered hashId and calculated hashId:
            if (!curHeader.hashId.contentEquals(cmpHash)) {
                Logger.debug {
                    """
                    |Current Hashes not equal:
                    |   ${curHeader.hashId.print}
                    |   -- and --
                    |   ${cmpHash.print}
                    """.trimMargin()
                }
                return false
            }
            val prevHeader = previousBlock.header
            // compare previous hashId and registered previous hashId
            if (!prevHeader.hashId.contentEquals(curHeader.previousHash)) {
                Logger.debug {
                    """
                    |Previous Hashes not equal:
                    |   ${prevHeader.hashId.print}
                    |   -- and --
                    |   ${curHeader.previousHash.print}
                    """.trimMargin()
                }
                return false
            }

            val hashTarget = currentBlock.coinbase.difficulty
            val curDiff = curHeader.hashId.difficulty
            if (curDiff > hashTarget) {
                Logger.debug {
                    "Unmined block: ${curHeader.hashId.print}"
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
        pw.getBlockByHeaderHash(
            chainHash, hash
        )

    /**
     * Takes a [blockheight] and returns a [LoadFailure]
     * over a [Block] with the provided [blockheight].
     */
    fun getBlockByHeight(
        blockheight: Long
    ): Outcome<Block, LoadFailure> =
        pw.getBlockByBlockHeight(
            chainHash, blockheight
        )

    /**
     * Takes the [hash] of a block and returns a [LoadFailure]
     * over a [BlockHeader] with the provided [hash].
     */
    fun getBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        pw.getBlockHeaderByHash(
            chainHash, hash
        )

    /**
     * Returns whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        (pw.getBlockByHeaderHash(
            chainHash, hash
        ) is Outcome.Ok<*>)

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [Block] previous to that which has [hash].
     */
    fun getPrevBlock(
        hash: Hash
    ): Outcome<Block, LoadFailure> =
        pw.getBlockByPrevHeaderHash(
            chainHash, hash
        )

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [BlockHeader] previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        pw.getBlockHeaderByPrevHeaderHash(
            chainHash, hash
        )


    /**
     * Returns an [Outcome] over the requested [Sequence] of [Block]
     * for the specified blockheight [range].
     */
    fun getBlockChunk(range: LongRange) =
        getBlockChunk(range.first, range.last)


    /**
     * Preferrable overload, returns an [Outcome] over
     * [Block]s for the specified blockheight interval, from
     * [startInclusive] to [endInclusive].
     */
    fun getBlockChunk(
        startInclusive: Long, endInclusive: Long
    ): Outcome<Sequence<Block>, LoadFailure> =
        pw.getBlockListByBlockHeightInterval(
            chainHash,
            startInclusive,
            endInclusive
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
            val recalcTrigger = ledgerParams.recalcTrigger
            if (validateBlock(it.hashId, block)) {
                if (lastRecalc == recalcTrigger) {
                    recalculateDifficulty(block)
                    lastRecalc = 0
                } else {
                    lastRecalc++
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
                    pw.persistEntity(
                        block,
                        BlockStorageAdapter
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
        if (block.header.previousHash
                .contentEquals(hash)
        ) {
            if (block.header.hashId.difficulty <=
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
        val fromHeight = cmp - ledgerParams.recalcTrigger
        val recalcBlock =
            pw.getBlockByBlockHeight(chainHash, fromHeight)
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
                    |   Cause: ${recalcBlock.failure.cause}
                    """.trimMargin()
                }
                difficultyTarget
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
        val recalcMult = LedgerConfiguration.RECALC_MULT
        val recalcDiv = LedgerConfiguration.RECALC_DIV
        val deltax = BigDecimal(ledgerParams.recalcTime - deltaStamp)
        val deltadiv = (deltax * recalcMult)
            .divideToIntegralValue(BigDecimal(ledgerParams.recalcTime))
            .toBigInteger()
        val difficulty = BigInteger(difficultyTarget.bytes)
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
    ): Outcome<BlockState, BlockFailure> {
        TODO()
    }


    companion object {
        val identity = Identity("")

        fun getOriginHeader(
            chainId: ChainId
        ): BlockHeader =
            StorageUnawareBlockHeader(
                chainId,
                LedgerHandle.getHasher(chainId.ledgerHash)!!,
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
            chainId: ChainId
        ): Block =
            LedgerHandle
                .getContainer(chainId.ledgerHash)!!
                .let {
                    StorageUnawareBlock(
                        sortedSetOf(),
                        StorageUnawareCoinbase(
                            INIT_DIFFICULTY,
                            0,
                            it
                        ),
                        getOriginHeader(chainId),
                        StorageUnawareMerkleTree(it.hasher)
                    ).also { bl ->
                        bl.plus(
                            Transaction(
                                chainId,
                                identity,
                                PhysicalData(
                                    BigDecimal.ZERO, BigDecimal.ZERO,
                                    DummyData.DUMMY
                                ),
                                it.hasher
                            )
                        )
                    }
                }

    }
}

