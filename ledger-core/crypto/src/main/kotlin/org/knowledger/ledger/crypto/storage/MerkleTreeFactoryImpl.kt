package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

object MerkleTreeFactoryImpl : MerkleTreeFactory {
    override fun create(
        hasher: Hashers,
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MutableMerkleTree = MerkleTreeImpl(
        hasher = hasher,
        _collapsedTree = collapsedTree.toMutableList(),
        _levelIndex = levelIndex.toMutableList()
    )

    override fun create(
        merkleTree: MerkleTree
    ): MutableMerkleTree = create(
        merkleTree.hasher, merkleTree.collapsedTree,
        merkleTree.levelIndex
    )

    override fun create(
        merkleTree: MutableMerkleTree
    ): MutableMerkleTree = create(
        hasher = merkleTree.hasher,
        collapsedTree = merkleTree.collapsedTree,
        levelIndex = merkleTree.levelIndex
    )

    override fun create(
        hasher: Hashers,
        data: Array<out Hashing>
    ): MutableMerkleTree =
        MerkleTreeImpl(hasher = hasher).also {
            it.rebuildMerkleTree(data)
        }

    override fun create(
        hasher: Hashers,
        primary: Hashing,
        data: Array<out Hashing>
    ): MutableMerkleTree =
        MerkleTreeImpl(hasher = hasher).also {
            it.rebuildMerkleTree(primary, data)
        }


}