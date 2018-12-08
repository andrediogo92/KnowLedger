package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.Storable
import pt.um.lei.masb.blockchain.persistance.getBlockByBlockHeight
import pt.um.lei.masb.blockchain.persistance.getBlockByHeaderHash
import pt.um.lei.masb.blockchain.persistance.getBlockByPrevHeaderHash
import pt.um.lei.masb.blockchain.persistance.getBlockHeaderByPrevHeaderHash
import pt.um.lei.masb.blockchain.persistance.getLatestBlock
import pt.um.lei.masb.blockchain.persistance.getLatestBlockHeader
import pt.um.lei.masb.blockchain.persistance.persistEntity
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.RingBuffer
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

class SideChain(
    val clazz: KClass<*>,
    val blockChainId: BlockChainId
) : Storable {


    @Transient
    private var cache: RingBuffer<Block> = RingBuffer(BlockChain.CACHE_SIZE)

    private var difficultyTarget: Difficulty = INIT_DIFFICULTY

    private var lastRecalc: Int = 0


    /**
     * @return The tail-end block of the blockchain.
     */
    val lastBlock: Block?
        get() = cache.peek() ?: getLatestBlock()


    /**
     * @return The tail-end blockheader in the blockchain.
     */
    val lastBlockHeader: BlockHeader?
        get() = cache.peek()
            ?.header
            ?: getLatestBlockHeader()


    override fun store(): OElement {
        TODO("store not implemented")
    }

    /**
     * Checks integrity of the entire cached blockchain.
     *
     * TODO: actually check entire blockchain in
     * ranges of [BlockChain.CACHE_SIZE] blocks.
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
            val cmpHash = curHeader.digest(crypter)
            // compare registered hash and calculated hash:
            if (curHeader.currentHash != cmpHash) {
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
            if (prevHeader.currentHash != curHeader.previousHash) {
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
            it.header.currentHash == hash
        } ?: getBlockByHeaderHash(hash)

    /**
     * @param blockheight Block height of block to fetch.
     * @return Block with provided blockheight, if it exists, else the null block.
     */
    fun getBlockByHeight(blockheight: Long): Block? =
        cache.find {
            it.header.blockheight == blockheight
        } ?: getBlockByBlockHeight(blockheight)

    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    fun getBlockHeaderByHash(hash: Hash): BlockHeader? =
        cache.find {
            it.header.currentHash == hash
        }?.header
            ?: getBlockHeaderByHash(hash)

    /**
     * Checks whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        cache.any {
            it.header.currentHash == hash
        } || getBlockByHeaderHash(hash) != null

    /**
     * Gets the block previous to that which has [hash].
     */
    fun getPrevBlock(hash: Hash): Block? =
        cache.find { h ->
            h.header.currentHash != hash
        } ?: getBlockByPrevHeaderHash(hash)

    /**
     * Gets the blockheader of the block previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(hash: Hash): BlockHeader? =
        cache.find { h ->
            h.header.currentHash != hash
        }?.header
            ?: getBlockHeaderByPrevHeaderHash(hash)

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
        if (block.header.previousHash == lastBlockHeader?.currentHash) {
            if (block.header.currentHash.toDifficulty() <=
                block.header.difficulty
            ) {
                if (block.verifyTransactions()) {
                    if (lastRecalc == BlockChain.RECALC_TRIGGER) {
                        recalculateDifficulty(block)
                        lastRecalc = 0
                    } else {
                        lastRecalc++
                    }
                    return persistEntity(block.store()) && cache.add(block)
                }
            }
        }
        return false
    }

    /**
     * Difficulty is recalculated based on timestamp
     * difference between [triggerBlock] at current blockheight
     * and Block at current blockheight - [RECALC_TRIGGER].
     *
     * This difference is measured as a percentage of
     * [RECALC_TIME] which is used to multiply by current
     * difficulty target.
     *
     * @returns The recalculated difficulty or the same
     *          difficulty if re-triggered erroneously.
     */
    private fun recalculateDifficulty(triggerBlock: Block): Difficulty {
        val cmp = triggerBlock.header.blockheight
        val cstamp = triggerBlock.header.timestamp.epochSecond
        val fromHeight = cmp - BlockChain.RECALC_TRIGGER
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
        val deltax = BigDecimal(BlockChain.RECALC_TIME - deltaStamp)
        val deltadiv = (deltax * BlockChain.RECALC_MULT)
            .divideToIntegralValue(BigDecimal(BlockChain.RECALC_TIME))
            .toBigInteger()
        val difficulty = BigInteger(difficultyTarget.toByteArray())
        val newDiff = difficulty + (difficulty * deltadiv)
        return padOrMax(newDiff / BlockChain.RECALC_DIV)
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
        return if (lastBlock != null) {
            val lh = lastBlock!!.header
            Block(
                blockChainId,
                lh.currentHash,
                difficultyTarget,
                lh.blockheight + 1
            )
        } else {
            logger.error { "Failure to fetch last block." }
            null
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
                blockChainId,
                prevHash,
                difficultyTarget,
                block.header.blockheight + 1
            )
        } else {
            logger.error { "Failure to fetch last block $prevHash." }
            null
        }
    }

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}