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
import pt.um.lei.masb.blockchain.persistance.results.PersistResult
import pt.um.lei.masb.blockchain.service.results.LoadResult
import pt.um.lei.masb.blockchain.utils.Failable
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
    val lastBlock: LoadResult<Block>
        get() = cache.peek()?.let {
            LoadResult.Success(it)
        } ?: pw.getLatestBlock(ledgerId)


    /**
     * @return The tail-end blockheader in the blockchain.
     */
    val lastBlockHeader: LoadResult<BlockHeader>
        get() = cache.peek()
            ?.header?.let {
            LoadResult.Success(it)
        } ?: pw.getLatestBlockHeader(ledgerId)


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
     * ranges of [ChainHandle.CACHE_SIZE] blocks.
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
            if (!curHeader.hash.contentEquals(cmpHash)) {
                logger.debug {
                    """
                    |Current Hashes not equal:
                    |   ${curHeader.hash.print()}
                    |   -- and --
                    |   ${cmpHash.print()}
                    """.trimMargin()
                }
                return false
            }
            val prevHeader = previousBlock.header
            // compare previous hash and registered previous hash
            if (!prevHeader.hash.contentEquals(curHeader.previousHash)) {
                logger.debug {
                    """
                    |Previous Hashes not equal:
                    |   ${prevHeader.hash.print()}
                    |   -- and --
                    |   ${curHeader.previousHash.print()}
                    """.trimMargin()
                }
                return false
            }

            val hashTarget = curHeader.difficulty
            val curDiff = curHeader.hash.toDifficulty()
            if (curDiff > hashTarget) {
                logger.debug {
                    "Unmined block: ${curHeader.hash.print()}"
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
    fun getBlock(hash: Hash): LoadResult<Block> =
        cache.find {
            it.header.hash
                .contentEquals(hash)
        }?.let {
            LoadResult.Success(it)
        } ?: pw.getBlockByHeaderHash(
            ledgerId,
            hash
        )

    /**
     * @param blockheight Block height of block to fetch.
     * @return Block with provided blockheight, if it exists, else the null block.
     */
    fun getBlockByHeight(blockheight: Long): LoadResult<Block> =
        cache.find {
            it.header.blockheight == blockheight
        }?.let {
            LoadResult.Success(it)
        } ?: pw.getBlockByBlockHeight(
            ledgerId,
            blockheight
        )

    /**
     * @param hash  Hash of block.
     * @return Block with provided hash if exists, else null.
     */
    fun getBlockHeaderByHash(hash: Hash): LoadResult<BlockHeader> =
        cache.find {
            it.header.hash
                .contentEquals(hash)
        }?.header?.let {
            LoadResult.Success(it)
        } ?: pw.getBlockHeaderByHash(
            ledgerId,
            hash
        )

    /**
     * Checks whether the block with [hash] exists.
     */
    fun hasBlock(hash: Hash): Boolean =
        cache.any {
            it.header.hash.contentEquals(hash)
        } || (pw.getBlockByHeaderHash(
            ledgerId,
            hash
        ) is LoadResult.Success)

    /**
     * Gets the block previous to that which has [hash].
     */
    fun getPrevBlock(hash: Hash): LoadResult<Block> =
        cache.find { h ->
            !h.header.hash.contentEquals(hash)
        }?.let {
            LoadResult.Success(it)
        } ?: pw.getBlockByPrevHeaderHash(
            ledgerId,
            hash
        )

    /**
     * Gets the blockheader of the block previous to that which has [hash].
     */
    fun getPrevBlockHeaderByHash(hash: Hash): LoadResult<BlockHeader> =
        cache.find { h ->
            !h.header.hash.contentEquals(hash)
        }?.header?.let {
            LoadResult.Success(it)
        } ?: pw.getBlockHeaderByPrevHeaderHash(
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
        return if (currentBlockheight == 1L) {
            validateBlock(getOriginHeader(ledgerId).hash, block)
        } else {
            when (lastBlockHeader) {
                is LoadResult.Success ->
                    validateBlock(
                        (lastBlockHeader as LoadResult.Success<BlockHeader>).data.hash,
                        block
                    )
                is LoadResult.QueryFailure -> false
                is LoadResult.NonExistentData -> false
                is LoadResult.NonMatchingCrypter -> false
                is LoadResult.UnregisteredCrypter -> false
                is LoadResult.UnrecognizedDataType -> false
            }
        }
    }

    private fun validateBlock(hash: Hash, block: Block): Boolean {
        if (block.header.previousHash
                .contentEquals(hash)
        ) {
            if (block.header.hash.toDifficulty() <=
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
                    ) is PersistResult.Success && cache.offer(block)
                }
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
     * @returns The recalculated difficulty or the same
     *          difficulty if re-triggered erroneously.
     */
    private fun recalculateDifficulty(
        triggerBlock: Block
    ): Difficulty {
        val cmp = triggerBlock.header.blockheight
        val cstamp = triggerBlock.header.timestamp.epochSecond
        val fromHeight = cmp - params.recalcTrigger
        val recalcBlock = pw.getBlockByBlockHeight(ledgerId, fromHeight)
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
                emptyHash(),
                emptyHash(),
                emptyHash(),
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
                        Ident(""),
                        PhysicalData(DummyData())
                    )
                )
            }

    }
}

