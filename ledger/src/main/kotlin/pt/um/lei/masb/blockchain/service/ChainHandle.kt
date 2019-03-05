package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.DummyData
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.ledger.*
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.RingBuffer
import java.math.BigDecimal
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ChainHandle internal constructor(
    private val pw: PersistenceWrapper,
    private val params: LedgerParams,
    val clazz: String,
    val ledgerId: Hash
) : Storable, ServiceHandle {

    @Transient
    private var cache: RingBuffer<Block> = RingBuffer(CACHE_SIZE)

    private var difficultyTarget: Difficulty =
        INIT_DIFFICULTY

    private var lastRecalc: Long = 0L

    private var currentBlockheight = 0L

    /**
     * @return The tail-end block of the blockchain.
     */
    val lastBlock: Block?
        get() = cache.peek()
            ?: pw.getLatestBlock(ledgerId)


    /**
     * @return The tail-end blockheader in the blockchain.
     */
    val lastBlockHeader: BlockHeader?
        get() = cache.peek()
            ?.header
            ?: pw.getLatestBlockHeader(ledgerId)


    internal constructor(
        pw: PersistenceWrapper,
        params: LedgerParams,
        clazz: String,
        blockChainId: Hash,
        difficulty: Difficulty,
        lastRecalc: Long,
        currentBlockheight: Long
    ) : this(
        pw,
        params,
        clazz,
        blockChainId
    ) {
        this.difficultyTarget = difficulty
        this.lastRecalc = lastRecalc
        this.currentBlockheight = currentBlockheight
    }

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("ChainHandle")
            .apply {
                this.setProperty(
                    "clazz",
                    clazz
                )
                this.setProperty(
                    "hash",
                    ledgerId
                )
                this.setProperty(
                    "difficultyTarget",
                    difficultyTarget.toByteArray()
                )
                this.setProperty(
                    "lastRecalc",
                    lastRecalc
                )
                this.setProperty(
                    "currentBlockheight",
                    currentBlockheight
                )
            }


    /**
     * Checks integrity of the entire cached blockchain.
     *
     * TODO: actually check entire blockchain in
     * ranges of [LedgerHandle.CACHE_SIZE] blocks.
     * @return Whether the chain is valid.
     */
    fun isChainValid(): Boolean {
        val blocks = cache.iterator()
        // Origin block is always the first block.
        var previousBlock = blocks.next()

        // loop through blockchain to check hashes:
        while (blocks.hasNext()) {
            val currentBlock = blocks.next()
            val curHeader = currentBlock.header
            val cmpHash = curHeader.digest(params.crypter)
            // compare registered hash and calculated hash:
            if (!curHeader.currentHash.contentEquals(cmpHash)) {
                logger.debug {
                    """
                    |Current Hashes not equal:
                    |   ${curHeader.currentHash}
                    |   -- and --
                    |   $cmpHash
                    """.trimMargin()
                }
                return false
            }
            val prevHeader = previousBlock.header
            // compare previous hash and registered previous hash
            if (!prevHeader.currentHash.contentEquals(curHeader.previousHash)) {
                logger.debug {
                    """
                    |Previous Hashes not equal:
                    |   ${prevHeader.currentHash}
                    |   -- and --
                    |   ${curHeader.previousHash}
                    """.trimMargin()
                }
                return false
            }

            val hashTarget = curHeader.difficulty
            val curDiff = curHeader.currentHash.toDifficulty()
            if (curDiff > hashTarget) {
                logger.debug {
                    "Unmined block: ${curHeader.currentHash}"
                }
                return false
            }

            previousBlock = currentBlock
        }
        return true
    }

    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    fun getBlock(hash: Hash): Block? =
        cache.find {
            it.header.currentHash
                .contentEquals(hash)
        } ?: pw.getBlockByHeaderHash(
            ledgerId,
            hash
        )

    /**
     * @param blockheight Block height of block to fetch.
     * @return Block with provided blockheight, if it exists, else the null block.
     */
    fun getBlockByHeight(blockheight: Long): Block? =
        cache.find {
            it.header.blockheight == blockheight
        } ?: pw.getBlockByBlockHeight(
            ledgerId,
            blockheight
        )

    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    fun getBlockHeaderByHash(hash: Hash): BlockHeader? =
        cache.find {
            it.header.currentHash
                .contentEquals(hash)
        }?.header
            ?: pw.getBlockHeaderByHash(
                ledgerId,
                hash
            )

    /**
     * Checks whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        cache.any {
            it.header.currentHash
                .contentEquals(hash)
        } || pw.getBlockByHeaderHash(
            ledgerId,
            hash
        ) != null

    /**
     * Gets the block previous to that which has [hash].
     */
    fun getPrevBlock(hash: Hash): Block? =
        cache.find { h ->
            !h.header.currentHash
                .contentEquals(hash)
        } ?: pw.getBlockByPrevHeaderHash(
            ledgerId,
            hash
        )

    /**
     * Gets the blockheader of the block previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(hash: Hash): BlockHeader? =
        cache.find { h ->
            !h.header.currentHash
                .contentEquals(hash)
        }?.header
            ?: pw.getBlockHeaderByPrevHeaderHash(
                ledgerId,
                hash
            )

    /**
     * Add [block] to blockchain if block is valid.
     *
     * May trigger difficulty recalculation.
     *
     * TODO: Verify coinbase.
     *
     * @return Whether block was successfully added.
     */
    fun addBlock(block: Block): Boolean {
        //Blockheight is 1.
        val lh = if (currentBlockheight == 1L) {
            return validateBlock(getOriginHeader(ledgerId).currentHash, block)
        } else {
            lastBlockHeader?.currentHash?.let {
                return validateBlock(it, block)
            }
        }
        return false
    }

    private fun validateBlock(hash: Hash, block: Block): Boolean {
        if (block.header.previousHash
                .contentEquals(hash)
        ) {
            if (block.header.currentHash.toDifficulty() <=
                block.header.difficulty
            ) {
                if (block.verifyTransactions()) {
                    if (lastRecalc == params.recalcTrigger) {
                        recalculateDifficulty(block)
                        lastRecalc = 0
                    } else {
                        lastRecalc++
                    }
                    return pw.persistEntity(
                        block
                    ) && cache.offer(block)
                }
            }
        }
        return false
    }

    /**
     * Difficulty is recalculated based on timestamp
     * difference between [triggerBlock] at current blockheight
     * and Block at current blockheight - [LedgerHandle.RECALC_TRIGGER].
     *
     * This difference is measured as a percentage of
     * [LedgerHandle.RECALC_TIME] which is used to multiply by current
     * difficulty target.
     *
     * @returns The recalculated difficulty or the same
     *          difficulty if re-triggered erroneously.
     */
    private fun recalculateDifficulty(
        triggerBlock: Block
    ): Difficulty {
        val cmp = triggerBlock.header.blockheight
        val cstamp = triggerBlock.header.timestamp.epochSecond
        val fromHeight = cmp - params.recalcTrigger
        val recalcBlock = lastBlock
        //BlockTransactions().getBlockByBlockHeight(fromHeight)
        return if (recalcBlock != null) {
            val pstamp = recalcBlock.header.timestamp.epochSecond
            val deltaStamp = cstamp - pstamp
            recalc(triggerBlock, recalcBlock, deltaStamp)
        } else {
            logger.error {
                """
                | Difficulty retrigger without 2048 blocks existent?
                |   Grab from Index: $fromHeight
                """.trimMargin()
            }
            difficultyTarget
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
        val difficulty = BigInteger(difficultyTarget.toByteArray())
        val newDiff = difficulty + (difficulty * deltadiv)
        return padOrMax(newDiff / RECALC_DIV)
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
     * Creates new empty [Block] with appropriate difficulty target
     * referencing the last known block.
     *
     * TODO: Rework logic to allow multiple concurrent blocks.
     *
     */
    fun newBlock(): Block? {
        return when {
            (lastBlockHeader != null && currentBlockheight != 0L) -> {
                val lh = lastBlockHeader
                currentBlockheight++
                Block(
                    ledgerId,
                    lh!!.currentHash,
                    difficultyTarget,
                    lh.blockheight + 1
                )
            }
            (lastBlockHeader == null && currentBlockheight == 0L) -> {
                currentBlockheight++
                Block(
                    ledgerId,
                    getOriginHeader(ledgerId).currentHash,
                    difficultyTarget,
                    currentBlockheight
                )
            }
            else -> {
                logger.error { "QueryFailure to fetch last block." }
                null
            }
        }
    }

    /**
     * Creates new empty [Block] with appropriate difficulty target
     * referencing the block with [prevHash].
     */
    fun newBlock(prevHash: Hash): Block? {
        val block = getBlock(prevHash)
        return if (block != null) {
            Block(
                ledgerId,
                prevHash,
                difficultyTarget,
                block.header.blockheight + 1
            )
        } else {
            logger.error { "QueryFailure to fetch last block $prevHash." }
            null
        }
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
                emptyHash(),
                emptyHash(),
                emptyHash(),
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
                        Ident(""),
                        PhysicalData(DummyData())
                    )
                )
            }

    }
}