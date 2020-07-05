package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace

internal data class StorageAwareMerkleTreeImpl(
    override val merkleTree: MutableMerkleTree
) : MutableMerkleTree by merkleTree, StorageAwareMerkleTree {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.HashList("nakedTree"),
            StoragePairs.Native("levelIndexes")
        )

    override var id: StorageID? = null

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun buildFromCoinbase(primary: Hashing) {
        merkleTree.buildFromCoinbase(primary)
        if (id != null) {
            invalidated.replace(0, collapsedTree)
        }
    }

    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        merkleTree.rebuildMerkleTree(data)
        if (id != null) {
            invalidated.replace(0, collapsedTree)
            invalidated.replace(1, levelIndex)
        }
    }

    override fun equals(other: Any?): Boolean =
        merkleTree == other

    override fun hashCode(): Int =
        merkleTree.hashCode()
}