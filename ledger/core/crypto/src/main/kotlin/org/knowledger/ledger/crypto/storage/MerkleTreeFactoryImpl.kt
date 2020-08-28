package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

class MerkleTreeFactoryImpl : MerkleTreeFactory {
    override fun create(
        hasher: Hashers, collapsedTree: List<Hash>, levelIndex: List<Int>,
    ): MerkleTreeImpl =
        MerkleTreeImpl(hasher, collapsedTree.toMutableList(), levelIndex.toMutableList())

    override fun create(merkleTree: MerkleTree): MerkleTreeImpl =
        create(merkleTree.hashers, merkleTree.collapsedTree, merkleTree.levelIndex)

    override fun create(merkleTree: MutableMerkleTree): MerkleTreeImpl =
        create(merkleTree as MerkleTree)

    override fun create(hasher: Hashers, data: Array<out Hashing>): MerkleTreeImpl =
        create(hasher).also { it.rebuildMerkleTree(data) }

    override fun create(
        hasher: Hashers, primary: Hashing, data: Array<out Hashing>,
    ): MerkleTreeImpl = create(hasher).also {
        it.rebuildMerkleTree(primary, data)
    }


}