package pt.um.masb.ledger

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Difficulty
import pt.um.masb.common.Hash
import pt.um.masb.common.Sizeable
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.data.MerkleTree

@JsonClass(generateAdapter = true)
data class Block(
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

    companion object : KLogging()

}
