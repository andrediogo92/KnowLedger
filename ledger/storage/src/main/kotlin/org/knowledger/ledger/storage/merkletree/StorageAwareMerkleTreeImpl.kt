package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal data class StorageAwareMerkleTreeImpl(
    override val merkleTree: MutableMerkleTree,
) : MutableMerkleTree by merkleTree, StorageAwareMerkleTree {
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.HashList("collapsedTree"),
        StoragePairs.Native("levelIndexes")
    )

    override var id: StorageElement? = null

    override fun buildFromPrimary(primary: Hashing) {
        merkleTree.buildFromPrimary(primary)
        if (id != null) {
            invalidated.replaceUnchecked(0, collapsedTree)
        }
    }

    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        merkleTree.rebuildMerkleTree(data)
        if (id != null) {
            invalidated.replaceUnchecked(0, collapsedTree)
            invalidated.replaceUnchecked(1, levelIndex)
        }
    }

    override fun equals(other: Any?): Boolean = merkleTree == other

    override fun hashCode(): Int = merkleTree.hashCode()
}