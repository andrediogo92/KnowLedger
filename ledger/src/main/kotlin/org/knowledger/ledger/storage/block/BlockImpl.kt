package org.knowledger.ledger.storage.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.indexed
import org.tinylog.kotlin.Logger

internal data class BlockImpl(
    override val innerTransactions: MutableSortedList<MutableTransaction>,
    override val coinbase: MutableCoinbase,
    override val header: MutableBlockHeader,
    override val merkleTree: MutableMerkleTree,
    internal var cachedSize: Int = innerTransactions.map { it.approximateSize }.sum()
) : MutableBlock, LedgerContract {

    @Suppress("UNCHECKED_CAST")
    override val transactions: SortedList<Transaction>
        get() = innerTransactions as SortedList<Transaction>

    /**
     * @return Binary serialized size of transactions.
     */
    override val approximateSize: Int
        get() = cachedSize

    override fun newExtraNonce() {
        coinbase.header.newNonce()
        merkleTree.buildFromCoinbase(coinbase.header)
        header.updateMerkleTree(merkleTree.hash)
    }

    override fun recalculateCachedSize() {
        cachedSize = innerTransactions.map { it.approximateSize }.sum()
    }

    override fun plus(transaction: MutableTransaction): Boolean {
        if (!checkBlockFull(transaction)) {
            innerTransactions.add(transaction)
            innerTransactions.indexed()
            cachedSize = approximateSize + transaction.approximateSize
            Logger.debug {
                "Transaction Successfully added to Block ${header.hash}"
            }
            return true
        }
        Logger.debug {
            "Transaction failed to process. Block full at $approximateSize bytes, size ${transactions.size}."
        }
        return false
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        coinbase.header.markForMining(blockheight, difficulty)
    }


    override fun updateHashes() {
        merkleTree.rebuildMerkleTree(
            coinbase.header, innerTransactions.toTypedArray()
        )
        header.updateMerkleTree(merkleTree.hash)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false

        if (innerTransactions != other.transactions) return false
        if (coinbase != other.coinbase) return false
        if (header != other.header) return false
        if (merkleTree != other.merkleTree) return false

        return true
    }

    override fun hashCode(): Int {
        var result = innerTransactions.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }


}