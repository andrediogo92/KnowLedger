package org.knowledger.ledger.storage.block

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal class StorageAwareBlockImpl(
    override val block: MutableBlock
) : MutableBlock by block, StorageAwareBlock {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.Linked<MutableBlockHeader>(
            "blockHeader", AdapterIds.BlockHeader
        ), StoragePairs.Linked<MutableCoinbase>(
            "coinbase", AdapterIds.Coinbase
        ), StoragePairs.Linked<MutableMerkleTree>(
            "merkleTree", AdapterIds.MerkleTree
        ), StoragePairs.LinkedList<MutableTransaction>(
            "transactions", AdapterIds.Transaction
        )
    )

    override fun newExtraNonce() {
        block.newExtraNonce()
        invalidateCoinbase()
    }

    override fun plus(transaction: MutableTransaction): Boolean {
        val result = block.plus(transaction)
        if (result && id != null) {
            invalidated.replaceUnchecked(3, mutableTransactions)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidated.replaceUnchecked(0, blockHeader)
            invalidated.replaceUnchecked(2, merkleTree)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        block.markForMining(blockheight, difficulty)
        invalidateCoinbase()
    }

    private fun invalidateCoinbase() {
        if (id != null) {
            invalidated.replaceUnchecked(1, coinbase)
        }
    }

    override fun equals(other: Any?): Boolean =
        block == other

    override fun hashCode(): Int =
        block.hashCode()
}