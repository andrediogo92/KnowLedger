package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

interface MerkleTreeFactory {
    fun create(
        hasher: Hashers, collapsedTree: List<Hash> = emptyList(),
        levelIndex: List<Int> = emptyList(),
    ): MutableMerkleTree

    fun create(merkleTree: MerkleTree): MutableMerkleTree

    fun create(merkleTree: MutableMerkleTree): MutableMerkleTree

    fun create(hasher: Hashers, data: Array<out Hashing>): MutableMerkleTree

    fun create(hasher: Hashers, primary: Hashing, data: Array<out Hashing>): MutableMerkleTree
}