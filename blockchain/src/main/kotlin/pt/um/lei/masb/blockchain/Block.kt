package pt.um.lei.masb.blockchain

import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.MerkleTree
import java.math.BigInteger
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Block<T : BlockChainData<T>>(
        val data: MutableList<Transaction<T>>,
        val coinbase: Coinbase<T>,
        val header: BlockHeader,
        private var _merkleTree: MerkleTree) : Sizeable {

    companion object : KLogging() {
        private var origins: MutableMap<Class<*>, Block<*>> = mutableMapOf()

        const val MAX_BLOCK_SIZE = 500
        const val MAX_MEM = 2097152
    }

    val merkleTree
        get() = _merkleTree

    @Transient
    private val classSize: Long = ClassLayout.parseClass(this::class.java)
        .instanceSize()

    @Transient
    private var headerSize: Long = 0

    @Transient
    private var transactionsSize: Long = 0

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible ballpark of 2MB + merkleTree graph size.
    @Transient
    private var merkleTreeSize: Long = ClassLayout.parseClass(MerkleTree::class.java)
        .instanceSize()


    constructor(blockChainId: BlockChainId,
                previousHash: String,
                difficulty: BigInteger,
                blockheight: Long) : this(mutableListOf(),
                                          Coinbase(blockChainId),
                                          BlockHeader(blockChainId, previousHash, difficulty, blockheight),
                                          MerkleTree()) {
        headerSize = header.approximateSize
    }

    /**
     * Attempt one nonce calculation.
     *
     * @param invalidate    Whether to invalidate the nonce and MerkleTree
     *                      in case block has changed.
     * @param time          Whether to invalidate block calculations due to
     *                      timestamp (every couple of seconds).
     * @return Whether the block was successfully mined.
     */
    fun attemptMineBlock(invalidate: Boolean, time: Boolean): Boolean {
        //Can't mine origin block.
        if (this == origins[this::class.typeParameters[0].javaClass]) {
            return false
        }
        if (invalidate && time) {
            _merkleTree = MerkleTree.buildMerkleTree(coinbase, data)
            header.merkleRoot = merkleTree.root
            header.timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .toInstant()
            header.zeroNonce()
        } else if (invalidate) {
            _merkleTree = MerkleTree.buildMerkleTree(coinbase, data)
            header.merkleRoot = merkleTree.root
            header.zeroNonce()
        } else if (time) {
            header.timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .toInstant()
            header.zeroNonce()
        }
        header.updateHash()
        //TODO convert difficulty into hash correctly.
        return if (header.currentHash < header.difficulty.toString()) {
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
     * <pw>
     * Checks if block is sized correctly.
     * <pw>
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    fun addTransaction(transaction: Transaction<T>): Boolean {
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
     * Transactions are sorted in descending order of data timestamp.
     *
     * @param transaction Transaction to insert in descending order.
     */
    private fun insertSorted(transaction: Transaction<T>) {
        data.add(transaction)
        data.sortByDescending { t -> t.data.instant }
    }

    override val approximateSize: Long
        get() = classSize + transactionsSize + headerSize

    /**
     * Recalculates the entire block size.
     * <p>
     * Is somewhat time consuming and only necessary if:
     * <ol>
     *  <li>    There is a need to calculate the effective block size after deserialization
     *  <li>    There is a need to calculate the effective block size after retrieval
     *          of a block from a database.
     * </ol>
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

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append('{')
            .append(' ')
            .append(header.toString())
            .append("Transactions: [")
            .append(System.lineSeparator())
        data.forEach { sb.append(it) }
        sb.append(" ]")
            .append(System.lineSeparator())
            .append('}')
        return sb.toString()
    }
}
