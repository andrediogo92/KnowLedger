package pt.um.masb.ledger.service

import mu.KLogging
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Difficulty.Companion.INIT_DIFFICULTY
import pt.um.masb.common.data.Difficulty.Companion.MAX_DIFFICULTY
import pt.um.masb.common.data.Difficulty.Companion.MIN_DIFFICULTY
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.results.Failable
import pt.um.masb.common.storage.results.QueryResult
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.data.DummyData
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.results.intoQuery
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
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
    private val pw: PersistenceWrapper,
    val params: LedgerParams,
    val clazz: String,
    val ledgerHash: Hash
) : ServiceHandle {

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
     * Returns a [LoadResult] for the tail-end
     * [Block] in the ledger.
     */
    val lastBlock: LoadResult<Block>
        get() = pw.getLatestBlock(ledgerHash)


    /**
     * Returns a [LoadResult] for the tail-end
     * [Blockheader] in the ledger.
     */
    val lastBlockHeader: LoadResult<BlockHeader>
        get() = pw.getLatestBlockHeader(ledgerHash)


    internal constructor(
        pw: PersistenceWrapper,
        params: LedgerParams,
        clazz: String,
        ledgerHash: Hash,
        difficulty: Difficulty,
        lastRecalc: Long,
        currentBlockheight: Long
    ) : this(
        pw,
        params,
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
     * ranges of [ChainHandle.CACHE_SIZE] blocks.
     *
     * Returns whether the chain is valid.
     */
    fun isChainValid(): QueryResult<Boolean> {
        var blocksLeft = true
        var valid = true
        var blockResult =
            pw.getBlockByBlockHeight(ledgerHash, 1)
        lateinit var previousLastBlock: Block
        var result: QueryResult<Boolean>
        result = if (blockResult !is LoadResult.Success) {
            valid = false
            blockResult.intoQuery()
        } else {
            previousLastBlock = blockResult.data
            QueryResult.Success(true)

        }
        var lowIndex = -CACHE_SIZE.toLong() + 2
        var highIndex = 2L
        while (blocksLeft && valid) {
            lowIndex += CACHE_SIZE.toLong()
            highIndex += CACHE_SIZE.toLong()
            val blocks =
                pw.getBlockListByBlockHeightInterval(
                    lowIndex,
                    highIndex,
                    ledgerHash
                )
            if (blocks is LoadListResult.Success) {
                valid = checkBlocks(
                    previousLastBlock, blocks.data.iterator()
                )
            } else {
                result = blocks.intoQuery()
                valid = false
            }
        }
        return result
    }

    private fun checkBlocks(
        lastBlock: Block, data: Iterator<Block>
    ): Boolean {
        // loop through ledger to check hashes:
        var previousBlock = lastBlock
        while (data.hasNext()) {
            val currentBlock = data.next()
            val curHeader = currentBlock.header
            val cmpHash = curHeader.digest(params.crypter)
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
     * Takes the [hash] of a block and returns a [LoadResult]
     * over a [Block] with the provided [hash].
     */
    fun getBlock(hash: Hash): LoadResult<Block> =
        pw.getBlockByHeaderHash(
            ledgerHash,
            hash
        )

    /**
     * Takes a [blockheight] and returns a [LoadResult]
     * over a [Block] with the provided [blockheight].
     */
    fun getBlockByHeight(blockheight: Long): LoadResult<Block> =
        pw.getBlockByBlockHeight(
            ledgerHash,
            blockheight
        )

    /**
     * Takes the [hash] of a block and returns a [LoadResult]
     * over a [BlockHeader] with the provided [hash].
     */
    fun getBlockHeaderByHash(hash: Hash): LoadResult<BlockHeader> =
        pw.getBlockHeaderByHash(
            ledgerHash,
            hash
        )

    /**
     * Returns whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        (pw.getBlockByHeaderHash(
            ledgerHash,
            hash
        ) is LoadResult.Success)

    /**
     * Takes the hash of a block and returns a [LoadResult] over
     * the [Block] previous to that which has [hash].
     */
    fun getPrevBlock(
        hash: Hash
    ): LoadResult<Block> =
        pw.getBlockByPrevHeaderHash(
            ledgerHash,
            hash
        )

    /**
     * Takes the hash of a block and returns a [LoadResult] over
     * the [BlockHeader] previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(
        hash: Hash
    ): LoadResult<BlockHeader> =
        pw.getBlockHeaderByPrevHeaderHash(
            ledgerHash,
            hash
        )


    /**
     * Returns a [LoadListResult] over the requested list of [Block]
     * for the specified blockheight [range].
     */
    fun getBlockChunk(range: LongRange) =
        getBlockChunk(range.first, range.endInclusive)


    /**
     * Preferrable overload, returns a [LoadListResult] over
     * [Blocks] for the specified blockheight interval, from
     * [startInclusive] to [endInclusive].
     */
    fun getBlockChunk(
        startInclusive: Long, endInclusive: Long
    ): LoadListResult<Block> =
        pw.getBlockListByBlockHeightInterval(
            startInclusive,
            endInclusive,
            ledgerHash
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
    fun addBlock(block: Block): QueryResult<Boolean> =
        lastBlockHeader.let {
            val recalcTrigger = params.recalcTrigger
            val valid = it.intoQuery {
                if (validateBlock(
                        this.hashId,
                        block
                    )
                ) {
                    if (lastRecalc == recalcTrigger) {
                        recalculateDifficulty(block)
                        lastRecalc = 0
                    } else {
                        lastRecalc++
                    }
                    true
                } else {
                    false
                }
            }
            if (valid is QueryResult.Success) {
                pw.persistEntity(
                    block,
                    BlockStorageAdapter()
                ).intoQuery {
                    true
                }
            } else {
                valid
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
     * and Block at current blockheight - [params]'s recalcTrigger.
     *
     * This difference is measured as a percentage of
     * [params]'s recalcTime which is used to multiply by current
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
        val fromHeight = cmp - params.recalcTrigger
        val recalcBlock = pw.getBlockByBlockHeight(ledgerHash, fromHeight)
        return when (recalcBlock) {
            is LoadResult.Success -> {
                val pstamp = recalcBlock.data.header.timestamp.epochSecond
                val deltaStamp = cstamp - pstamp
                recalc(triggerBlock, recalcBlock.data, deltaStamp)
            }
            is Failable -> {
                logger.error {
                    """
                    | Difficulty retrigger without 2048 blocks existent?
                    |   Grab from Index: $fromHeight
                    |   Cause: ${recalcBlock.cause}
                    """.trimMargin()
                }
                difficultyTarget
            }
            else -> {
                logger.error {
                    """
                    | Difficulty retrigger without 2048 blocks existent?
                    |   Grab from Index: $fromHeight
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
        val deltax = BigDecimal(params.recalcTime - deltaStamp)
        val deltadiv = (deltax * RECALC_MULT)
            .divideToIntegralValue(BigDecimal(params.recalcTime))
            .toBigInteger()
        val difficulty = BigInteger(difficultyTarget.bytes)
        val newDiff = difficulty + (difficulty * deltadiv)
        return padOrMax(Difficulty(newDiff / RECALC_DIV))
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
        const val CACHE_SIZE = 40

        val RECALC_DIV = BigInteger("10000000000000")
        val RECALC_MULT = BigDecimal("10000000000000")


        fun getOriginHeader(
            blockChainId: Hash
        ): BlockHeader =
            BlockHeader(
                blockChainId,
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
            Block(
                mutableListOf(),
                Coinbase(),
                getOriginHeader(blockChainId),
                MerkleTree()
            ).apply {
                this.addTransaction(
                    Transaction(
                        Identity(""),
                        PhysicalData(DummyData.DUMMY)
                    )
                )
            }

    }
}

