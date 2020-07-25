package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

class MerkleTreeFactoryImpl : MerkleTreeFactory {
    override fun create(
        hasher: Hashers,
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTreeImpl = MerkleTreeImpl(
        hashers = hasher,
        _collapsedTree = collapsedTree.toMutableList(),
        _levelIndex = levelIndex.toMutableList()
    )

    override fun create(
        merkleTree: MerkleTree
    ): MerkleTreeImpl = create(
        merkleTree.hashers, merkleTree.collapsedTree,
        merkleTree.levelIndex
    )

    override fun create(
        merkleTree: MutableMerkleTree
    ): MerkleTreeImpl = create(
        hasher = merkleTree.hashers,
        collapsedTree = merkleTree.collapsedTree,
        levelIndex = merkleTree.levelIndex
    )

    override fun create(
        hasher: Hashers, data: Array<out Hashing>
    ): MerkleTreeImpl =
        MerkleTreeImpl(hashers = hasher).also {
            it.rebuildMerkleTree(data)
        }

    override fun create(
        hasher: Hashers, primary: Hashing, data: Array<out Hashing>
    ): MerkleTreeImpl = MerkleTreeImpl(hasher).also {
        it.rebuildMerkleTree(primary, data)
    }


}