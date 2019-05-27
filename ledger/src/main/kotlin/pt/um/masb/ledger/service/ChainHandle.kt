package pt.um.masb.ledger.service

import mu.KLogging
import pt.um.masb.common.config.LedgerConfiguration
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Difficulty.Companion.INIT_DIFFICULTY
import pt.um.masb.common.data.Difficulty.Companion.MAX_DIFFICULTY
import pt.um.masb.common.data.Difficulty.Companion.MIN_DIFFICULTY
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.data.DummyData
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.intoQuery
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.adapters.BlockStorageAdapter
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime

/**
 * A facade into useful methods for managing a
 * unique chain in the ledger represented by the [ledgerHash].
 */
data class ChainHandle internal constructor(
    val clazz: String,
    val ledgerHash: Hash
) : ServiceHandle {
    private val pw: PersistenceWrapper
    private val hasher: Hasher
    val ledgerParams: LedgerParams
    val coinbaseParams: CoinbaseParams

    init {
        val container = LedgerHandle.getContainer(
            ledgerHash
        )!!
        pw = container.persistenceWrapper
        hasher = container.hasher
        ledgerParams = container.ledgerParams
        coinbaseParams = container.coinbaseParams
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
        get() = pw.getLatestBlock(ledgerHash)


    /**
     * Returns a [LoadFailure] for the tail-end
     * [BlockHeader] in the ledger.
     */
    val lastBlockHeader: Outcome<BlockHeader, LoadFailure>
        get() = pw.getLatestBlockHeader(ledgerHash)


    internal constructor(
        clazz: String,
        ledgerHash: Hash,
        difficulty: Difficulty,
        lastRecalc: Long,
        currentBlockheight: Long
    ) : this(
        clazz,
        ledgerHash
    ) {
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
    data
    )
    header.merkleRoot = merkleTree.root
    header.timestamp = ZonedDateTime
    .now(ZoneOffset.UTC)
    .toInstant()
    header.nonce = 0
    } else if (invalidate) {
    merkleTree = MerkleTree.buildMerkleTree(
    coinbase,
    data
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
            pw.getBlockByBlockHeight(ledgerHash, 1)
        lateinit var previousLastBlock: Block
        var failure: Outcome<Boolean, QueryFailure>
        failure = when (blockResult) {
            is Outcome.Error -> {
                valid = false
                Outcome.Error(blockResult.failure.intoQuery())
            }
            is Outcome.Ok -> {
                previousLastBlock = blockResult.data
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
                    ledgerHash,
                    lowIndex,
                    highIndex
                )
            when (blocks) {
                is Outcome.Ok -> {
                    valid = checkBlocks(
                        previousLastBlock, blocks.data.iterator()
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
                logger.debug {
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
                logger.debug {
                    """
                    |Previous Hashes not equal:
                    |   ${prevHeader.hashId.print}
                    |   -- and --
                    |   ${curHeader.previousHash.print}
                    """.trimMargin()
                }
                return false
            }

            val hashTarget = curHeader.difficulty
            val curDiff = curHeader.hashId.difficulty
            if (curDiff > hashTarget) {
                logger.debug {
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
            ledgerHash, hash
        )

    /**
     * Takes a [blockheight] and returns a [LoadFailure]
     * over a [Block] with the provided [blockheight].
     */
    fun getBlockByHeight(
        blockheight: Long
    ): Outcome<Block, LoadFailure> =
        pw.getBlockByBlockHeight(
            ledgerHash, blockheight
        )

    /**
     * Takes the [hash] of a block and returns a [LoadFailure]
     * over a [BlockHeader] with the provided [hash].
     */
    fun getBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        pw.getBlockHeaderByHash(
            ledgerHash, hash
        )

    /**
     * Returns whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        (pw.getBlockByHeaderHash(
            ledgerHash, hash
        ) is Outcome.Ok<*, *>)

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [Block] previous to that which has [hash].
     */
    fun getPrevBlock(
        hash: Hash
    ): Outcome<Block, LoadFailure> =
        pw.getBlockByPrevHeaderHash(
            ledgerHash, hash
        )

    /**
     * Takes the hash of a block and returns a [LoadFailure] over
     * the [BlockHeader] previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(
        hash: Hash
    ): Outcome<BlockHeader, LoadFailure> =
        pw.getBlockHeaderByPrevHeaderHash(
            ledgerHash, hash
        )


    /**
     * Returns an [Outcome] over the requested [Sequence] of [Block]
     * for the specified blockheight [range].
     */
    fun getBlockChunk(range: LongRange) =
        getBlockChunk(range.first, range.endInclusive)


    /**
     * Preferrable overload, returns an [Outcome] over
     * [Block]s for the specified blockheight interval, from
     * [startInclusive] to [endInclusive].
     */
    fun getBlockChunk(
        startInclusive: Long, endInclusive: Long
    ): Outcome<Sequence<Block>, LoadFailure> =
        pw.getBlockListByBlockHeightInterval(
            ledgerHash,
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
        lastBlockHeader.mapSuccess {
            val recalcTrigger = ledgerParams.recalcTrigger
            if (validateBlock(this.data.hash, block)) {
                if (lastRecalc == recalcTrigger) {
                    recalculateDifficulty(block)
                    lastRecalc = 0
                } else {
                    lastRecalc++
                }
                Outcome.Ok<Boolean, LoadFailure>(true)
            } else {
                Outcome.Ok(false)
            }
        }.mapToNew(
            {
                Outcome.Error<Boolean, QueryFailure>(
                    this.failure.intoQuery()
                )
            },
            {
                if (this.data) {
                    pw.persistEntity(
                        block,
                        BlockStorageAdapter
                    ).flatMapSuccess {
                        true
                    }
                } else {
                    Outcome.Ok(this.data)
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
                block.header.difficulty
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
        val cmp = triggerBlock.header.blockheight
        val cstamp = triggerBlock.header.timestamp.epochSecond
        val fromHeight = cmp - ledgerParams.recalcTrigger
        val recalcBlock =
            pw.getBlockByBlockHeight(ledgerHash, fromHeight)
        return when (recalcBlock) {
            is Outcome.Ok<Block, *> -> {
                val pstamp = recalcBlock.data.header.timestamp.epochSecond
                val deltaStamp = cstamp - pstamp
                recalc(triggerBlock, recalcBlock.data, deltaStamp)
            }
            is Outcome.Error<Block, LoadFailure> -> {
                logger.error {
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
                logger.error {
                    "Difficulty not within expected bounds: $calcDiff"
                }
                calcDiff
            }
        }
    }

    /**
     * Add a transaction to the transaction pool.
     *
     */
    fun addTransaction(t: Transaction) {
        TODO()
    }


    companion object : KLogging() {
        val identity = Identity("")

        fun getOriginHeader(
            blockChainId: Hash
        ): BlockHeader =
            BlockHeader(
                blockChainId,
                LedgerHandle.getHasher(blockChainId)!!,
                MAX_DIFFICULTY,
                0,
                emptyHash,
                emptyHash,
                emptyHash,
                BlockParams(),
                ZonedDateTime
                    .of(
                        2018,
                        3,
                        13,
                        0,
                        0,
                        0,
                        0,
                        ZoneOffset.UTC
                    )
                    .toInstant(),
                0.toLong()
            )

        fun getOriginBlock(
            blockChainId: Hash
        ): Block =
            LedgerHandle.getContainer(blockChainId)!!.let {
                Block(
                    mutableListOf(),
                    Coinbase(it),
                    getOriginHeader(blockChainId),
                    MerkleTree(it.hasher)
                ).apply {
                    this.addTransaction(
                        Transaction(
                            identity,
                            PhysicalData(DummyData.DUMMY),
                            it.hasher
                        )
                    )
                }
            }

    }
}

