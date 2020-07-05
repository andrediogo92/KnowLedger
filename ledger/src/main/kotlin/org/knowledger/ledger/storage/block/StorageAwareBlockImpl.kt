package org.knowledger.ledger.storage.block

import kotlinx.serialization.Transient
import org.knowledger.ledger.adapters.AdapterCollection
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace

internal class StorageAwareBlockImpl(
    adapterCollection: AdapterCollection,
    override val block: MutableBlock
) : MutableBlock by block, StorageAwareBlock {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Linked(
                "header",
                adapterCollection.blockHeaderStorageAdapter
            ), StoragePairs.Linked(
                "coinbase",
                adapterCollection.coinbaseHeaderStorageAdapter
            ), StoragePairs.Linked(
                "merkleTree",
                adapterCollection.merkleTreeStorageAdapter
            ), StoragePairs.LinkedList(
                "data",
                adapterCollection.transactionStorageAdapter
            )
        )


    @Transient
    override var id: StorageID? = null

    override fun newExtraNonce() {
        block.newExtraNonce()
        invalidateCoinbase()
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun plus(transaction: MutableTransaction): Boolean {
        val result = block.plus(transaction)
        if (result && id != null) {
            invalidated.replace(3, transactions)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(2, merkleTree)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        block.markForMining(blockheight, difficulty)
        invalidateCoinbase()
    }

    private fun invalidateCoinbase() {
        if (id != null) {
            invalidated.replace(1, coinbase)
        }
    }

    override fun equals(other: Any?): Boolean =
        block == other

    override fun hashCode(): Int =
        block.hashCode()
}