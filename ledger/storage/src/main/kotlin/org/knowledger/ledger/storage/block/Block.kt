package org.knowledger.ledger.storage.block

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.data.Sizeable
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction

interface Block : Sizeable, LedgerContract, Comparable<Block> {
    val miningReady: Boolean
        get() {
            val blockParams = blockHeader.blockParams
            return transactions.size >= blockParams.blockLength / 4 ||
                   approximateSize >= blockParams.blockMemorySize / 2
        }

    val full: Boolean
        get() {
            val blockParams = blockHeader.blockParams
            return transactions.size == blockParams.blockLength ||
                    approximateSize >= blockParams.blockMemorySize
        }

    /**
     * @return Binary serialized size of transactions + coinbase.
     */
    val effectiveSize: Int
        get() = approximateSize + coinbase.approximateSize

    val blockHeader: BlockHeader
    val coinbase: Coinbase
    val merkleTree: MerkleTree
    val transactions: SortedList<Transaction>

    /**
     * @return Whether block would be full by adding [newTransaction].
     */
    fun checkBlockFull(newTransaction: Transaction): Boolean {
        val cumulativeSize: Int = newTransaction.approximateSize + effectiveSize
        val blockParams = blockHeader.blockParams
        return cumulativeSize >= blockParams.blockMemorySize || transactions.size >= blockParams.blockLength
    }

    fun verifyTransactions(): Boolean =
        merkleTree.verifyBlockTransactions(coinbase.coinbaseHeader, transactions.toTypedArray())

    override fun compareTo(other: Block): Int =
        other.blockHeader.hash.compareTo(blockHeader.hash)
}
