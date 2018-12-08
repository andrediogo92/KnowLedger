package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.Storable
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Block(
    private val _data: MutableList<Transaction>,
    val coinbase: Coinbase,
    val header: BlockHeader,
    private var _merkleTree: MerkleTree
) : Sizeable, Storable {


    val data: List<Transaction>
        get() = _data

    val merkleTree
        get() = _merkleTree

    @Transient
    private val classSize: Long =
        ClassLayout.parseClass(this::class.java)
            .instanceSize()

    @Transient
    private var headerSize: Long = 0

    @Transient
    private var transactionsSize: Long = 0

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible ballpark of 2MB + merkleRoot graph size.
    @Transient
    private var merkleTreeSize: Long = ClassLayout.parseClass(MerkleTree::class.java)
        .instanceSize()


    override val approximateSize: Long
        get() = classSize + transactionsSize + headerSize

    constructor(
        blockChainId: BlockChainId,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long
    ) : this(
        mutableListOf(),
        Coinbase(),
        BlockHeader(
            blockChainId,
            previousHash,
            difficulty,
            blockheight
        ),
        MerkleTree()
    ) {
        headerSize = header.approximateSize
    }


    override fun store(): OElement {
        TODO("not implemented")
    }

    /**
     * Attempt one nonce calculation.
     *
     * @param invalidate    Whether to invalidate the nonce and MerkleTree
     *                      in case block has changed.
     * @param time          Whether to invalidate block calculations due to
     *                      timestamp (every couple of seconds).
     * @param clazz         The block's effective underlying class.
     * @return Whether the block was successfully mined.
     */
    fun attemptMineBlock(
        invalidate: Boolean,
        time: Boolean
    ): Boolean {
        //Can't mine origin block.
        if (this == origins[header.blockChainId]) {
            return false
        }
        if (invalidate && time) {
            _merkleTree = MerkleTree.buildMerkleTree(coinbase, data)
            header._merkleRoot = merkleTree.root
            header._timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .toInstant()
            header.zeroNonce()
        } else if (invalidate) {
            _merkleTree = MerkleTree.buildMerkleTree(coinbase, data)
            header._merkleRoot = merkleTree.root
            header.zeroNonce()
        } else if (time) {
            header._timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .toInstant()
            header.zeroNonce()
        }
        header.updateHash()
        val curDiff = header.currentHash.toDifficulty()
        return if (curDiff < header.difficulty) {
            logger.info { "Block Mined!!! : ${header.currentHash}" }
            logger.info { "Block contains: ${toString()}" }
            true
        } else {
            header.incNonce()
            false
        }
    }

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    fun addTransaction(transaction: Transaction): Boolean {
        val transactionSize = transaction.approximateSize
        if (transactionsSize + headerSize + classSize + merkleTreeSize + transactionSize < MAX_MEM) {
            if (data.size < MAX_BLOCK_SIZE) {
                if (transaction.processTransaction()) {
                    insertSorted(transaction)
                    transactionsSize += transactionSize
                    logger.info { "Transaction Successfully added to Block" }
                    return true
                }
            }
        }
        logger.info { "Transaction failed to process. Discarded." }
        return false
    }

    /**
     * Transactions are sorted in descending order of data _timestamp.
     *
     * @param transaction Transaction to insert in descending order.
     */
    private fun insertSorted(transaction: Transaction) {
        _data.add(transaction)
        _data.sortByDescending { t -> t.data.instant }
    }

    /**
     * Recalculates the entire block size.
     *
     * Is somewhat time consuming and only necessary if:
     *
     *      1. There is a need to calculate the effective block size after deserialization
     *      2. There is a need to calculate the effective block size after retrieval
     *         of a block from a database.
     */
    fun resetApproximateSize() {
        headerSize = header.approximateSize
        transactionsSize = data.fold(0.toLong()) { acc, transaction ->
            acc + transaction.approximateSize
        }
        merkleTreeSize = merkleTree.approximateSize
    }

    fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(coinbase, data)
    }

    override fun toString(): String =
        StringBuilder().let { sb: StringBuilder
            ->
            sb.append(
                """
                | Block: {
                |   $coinbase
                |   $header
                |   Transactions: [
                """.trimMargin()
            )
            data.forEach {
                sb.append(
                    """
                    |       $it
                    """.trimMargin()
                )
            }
            sb.append(
                """
                |   ]
                | }
                """.trimMargin()
            )
            sb.toString()
        }

    companion object : KLogging() {
        private var origins: MutableMap<BlockChainId, Block> = mutableMapOf()

        const val MAX_BLOCK_SIZE = 500
        const val MAX_MEM = 2097152
    }

}
