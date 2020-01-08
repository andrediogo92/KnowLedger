package org.knowledger.ledger.storage.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.updateLinked

@Serializable
@SerialName("StorageBlockWrapper")
internal data class StorageAwareBlock(
    internal val block: BlockImpl
) : Block by block,
    StorageAware<Block> {
    @Transient
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Linked("header", BlockHeaderStorageAdapter),
            StoragePairs.Linked("coinbase", CoinbaseStorageAdapter),
            StoragePairs.Linked("merkleTree", MerkleTreeStorageAdapter),
            StoragePairs.LinkedSet("data", TransactionStorageAdapter)
        )

    @Transient
    override var id: StorageID? = null

    override fun newNonce(): BlockHeader {
        block.newNonce()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, coinbase)
            invalidated.replace(2, merkleTree)
        }
        return header
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun plus(transaction: Transaction): Boolean {
        val result = block + transaction
        if (result && id != null) {
            invalidated.replace(3, transactions)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, merkleTree)
        }
    }

    override fun clone(): Block {
        return BlockImpl(
            transactions.toSortedSet(),
            coinbase.clone(),
            header.clone(),
            merkleTree.clone()
        )
    }

    override fun equals(other: Any?): Boolean =
        block == other

    override fun hashCode(): Int =
        block.hashCode()
}