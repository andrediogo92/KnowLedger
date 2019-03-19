package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import java.time.ZoneOffset
import java.time.ZonedDateTime

class Block(
    val data: MutableList<Transaction>,
    val coinbase: Coinbase,
    val header: BlockHeader,
    var merkleTree: MerkleTree
) : Sizeable, Storable, LedgerContract {

    @Transient
    private val classSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()

    @Transient
    private var headerSize: Long = 0

    @Transient
    private var transactionsSize: Long = 0

    //Consider only the class size contribution to size.
    //Makes the total block size in the possible
    // ballpark of 2MB + merkleRoot graph size.
    @Transient
    private var merkleTreeSize: Long =
        ClassLayout
            .parseClass(MerkleTree::class.java)
            .instanceSize()


    override val approximateSize: Long
        get() = classSize +
                transactionsSize +
                headerSize +
                merkleTreeSize


    constructor(
        ledgerId: Hash,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long,
        params: BlockParams
    ) : this(
        mutableListOf(),
        Coinbase(),
        BlockHeader(
            ledgerId,
            previousHash,
            difficulty,
            blockheight,
            params
        ),
        MerkleTree()
    ) {
        headerSize = header.approximateSize
    }


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Block")
            .apply {
                setProperty(
                    "data",
                    data.map { it.store(session) })
                setProperty(
                    "coinbase",
                    coinbase.store(session)
                )
                setProperty(
                    "header",
                    header.store(session)
                )
                setProperty(
                    "merkleTree",
                    merkleTree.store(session)
                )
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
    fun attemptMineBlock(
        invalidate: Boolean,
        time: Boolean
    ): Boolean {
        //Can't mine origin block.
        if (this == origins[header.ledgerId]) {
            return false
        }
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
        val curDiff = header.hash.toDifficulty()
        return if (curDiff < header.difficulty) {
            logger.info {
                "Block Mined!!! : ${header.hash}"
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
        if (approximateSize +
            transactionSize < header.params.blockMemSize
        ) {
            if (data.size < header.params.blockLength) {
                if (transaction.processTransaction()) {
                    insertSorted(transaction)
                    transactionsSize += transactionSize
                    logger.info {
                        "Transaction Successfully added to Block"
                    }
                    return true
                }
            }
        }
        logger.info {
            "Transaction failed to process. Discarded."
        }
        return false
    }

    /**
     * Transactions are sorted in descending order of data internalTimestamp.
     *
     * @param transaction Transaction to insert in descending order.
     */
    private fun insertSorted(
        transaction: Transaction
    ) {
        data.add(transaction)
        data.sortByDescending {
            it.data.instant
        }
    }

    /**
     * Recalculates the entire block size.
     *
     * Is somewhat time consuming and only necessary if:
     *
     * 1. There is a need to calculate the effective block size after deserialization;
     * 2. There is a need to calculate the effective block size after retrieval
     *         of a block from a database.
     */
    fun resetApproximateSize() {
        headerSize = header.approximateSize
        transactionsSize = data.fold(
            0.toLong()
        ) { acc, transaction ->
            acc + transaction.approximateSize
        }
        merkleTreeSize = merkleTree.approximateSize
    }

    fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(
            coinbase,
            data
        )
    }

    override fun toString(): String =
        StringBuilder().let { sb: StringBuilder
            ->
            sb.append(
                """
                | Block: {
                |$coinbase
                |$header
                |   Transactions: [
                |
                """.trimMargin()
            )
            data.forEach {
                sb.append(
                    """$it,
                    |
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

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is Block)
            return false

        if (!data.containsAll(
                other.data
            )
        )
            return false
        if (data.size != other.data.size)
            return false
        if (coinbase != other.coinbase)
            return false
        if (header != other.header)
            return false
        if (merkleTree != other.merkleTree)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }

    companion object : KLogging() {
        private var origins =
            mutableMapOf<Hash, Block>()
    }

}
