package org.knowledger.ledger.storage.block

import org.knowledger.collections.SortedList
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction

interface Block : Sizeable, LedgerContract {
    val miningReady: Boolean
        get() {
            val blockParams = header.params
            return transactions.size >= blockParams.blockLength / 4 ||
                    approximateSize >= blockParams.blockMemorySize / 2
        }

    val full: Boolean
        get() {
            val blockParams = header.params
            return transactions.size == blockParams.blockLength ||
                    approximateSize >= blockParams.blockMemorySize
        }

    /**
     * @return Binary serialized size of transactions + coinbase.
     */
    val effectiveSize: Int
        get() = approximateSize + coinbase.approximateSize

    val transactions: SortedList<Transaction>
    val coinbase: Coinbase
    val header: BlockHeader
    val merkleTree: MerkleTree

    /**
     * @return Whether block would be full by adding [newTransaction].
     */
    fun checkBlockFull(newTransaction: Transaction): Boolean {
        val cumulativeSize: Int = newTransaction.approximateSize + effectiveSize
        val blockParams = header.params
        return cumulativeSize >= blockParams.blockMemorySize || transactions.size >= blockParams.blockLength
    }

    fun verifyTransactions(): Boolean =
        merkleTree.verifyBlockTransactions(
            coinbase.header, transactions.toTypedArray()
        )

}
