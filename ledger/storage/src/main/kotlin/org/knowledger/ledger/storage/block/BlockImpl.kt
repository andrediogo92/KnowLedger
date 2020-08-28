package org.knowledger.ledger.storage.block

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.mutations.indexed
import org.tinylog.kotlin.Logger

internal data class BlockImpl(
    override val blockHeader: MutableBlockHeader,
    override val coinbase: MutableCoinbase,
    override val merkleTree: MutableMerkleTree,
    override val mutableTransactions: MutableSortedList<MutableTransaction>,
    internal var cachedSize: Int =
        mutableTransactions.map(MutableTransaction::approximateSize).sum(),
) : MutableBlock, LedgerContract {

    @Suppress("UNCHECKED_CAST")
    override val transactions: SortedList<Transaction>
        get() = mutableTransactions as SortedList<Transaction>

    /**
     * @return Binary serialized size of transactions.
     */
    override val approximateSize: Int get() = cachedSize

    override fun newExtraNonce() {
        coinbase.coinbaseHeader.newNonce()
        merkleTree.buildFromPrimary(coinbase.coinbaseHeader)
        blockHeader.updateMerkleRoot(merkleTree.hash)
    }

    override fun recalculateCachedSize() {
        cachedSize = mutableTransactions.map(MutableTransaction::approximateSize).sum()
    }

    override fun plus(transaction: MutableTransaction): Boolean =
        if (mutableTransactions.add(transaction)) {
            mutableTransactions.indexed()
            cachedSize = approximateSize + transaction.approximateSize
            Logger.debug {
                "Transaction Successfully added to Block ${blockHeader.hash}"
            }
            true
        } else {
            Logger.debug { "Duplicate Transaction ${transaction.hash}" }
            false
        }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        coinbase.coinbaseHeader.markForMining(blockheight, difficulty)
    }


    override fun updateHashes() {
        merkleTree.rebuildMerkleTree(coinbase.coinbaseHeader, mutableTransactions.toTypedArray())
        blockHeader.updateMerkleRoot(merkleTree.hash)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false

        if (mutableTransactions != other.transactions) return false
        if (coinbase != other.coinbase) return false
        if (blockHeader != other.blockHeader) return false
        if (merkleTree != other.merkleTree) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mutableTransactions.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + blockHeader.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }


}