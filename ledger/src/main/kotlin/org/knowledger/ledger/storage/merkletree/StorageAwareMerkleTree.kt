package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareMerkleTree(
    internal val merkleTree: MerkleTreeImpl
) : MerkleTree by merkleTree, StorageAware<MerkleTree> {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.HashList("nakedTree"),
            StoragePairs.Native("levelIndexes")
        )

    override var id: StorageID? = null

    internal constructor(
        hasher: Hashers
    ) : this(
        merkleTree = MerkleTreeImpl(hasher = hasher)
    )

    internal constructor(
        hasher: Hashers,
        coinbase: Hashing,
        data: Array<out Hashing>
    ) : this(hasher = hasher) {
        rebuildMerkleTree(
            coinbase = coinbase, data = data
        )
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)


    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        merkleTree.rebuildMerkleTree(data)
        if (id != null) {
            invalidated.replace(0, merkleTree.collapsedTree)
            invalidated.replace(1, merkleTree.levelIndex)
        }
    }

    override fun equals(other: Any?): Boolean =
        merkleTree == other

    override fun hashCode(): Int =
        merkleTree.hashCode()
}