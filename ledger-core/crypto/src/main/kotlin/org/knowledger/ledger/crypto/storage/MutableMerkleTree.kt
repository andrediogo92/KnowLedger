package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

interface MutableMerkleTree : MerkleTree {
    /**
     * Uses the provided [hasher] to rebuild the [MerkleTree] in-place with the new [hasher].
     * If the [hasher] provided is already the one used it's a no-op.
     */
    fun changeHasher(hasher: Hashers)

    /**
     * Uses the provided [diff] array of changed elements and their indexes in [diffIndexes]
     * to regenerate only the changed parts of the MerkleTree.
     */
    fun buildDiff(diff: Array<out Hashing>, diffIndexes: Array<Int>)


    /**
     * Builds a [MerkleTree] collapsed in a heap for easy navigability from bottom up.
     *
     * Initializes the first tree layer, which is the transaction layer,
     * sets a correspondence from each hashId to its index and starts a build loop,
     * building all subsequent layers.
     *
     * Takes [data] as the transactions in the block and outputs the full
     * corresponding [MerkleTree] for their hashes, or an empty [MerkleTree]
     * if supplied with empty [data].
     */
    fun rebuildMerkleTree(data: Array<out Hashing>)

    /**
     * Builds a [MerkleTree] collapsed in a heap for easy navigability from bottom up.
     *
     * Initializes the first tree layer, which is the transaction layer,
     * sets a correspondence from each hashId to its index and starts a build loop,
     * building all subsequent layers.
     *
     * Takes [data] as the transactions in the block + the special [primary] transaction's
     * hashes and outputs the full corresponding [Merkle Tree], or an empty [MerkleTree] if
     * supplied with empty [data].
     */
    fun rebuildMerkleTree(
        primary: Hashing, data: Array<out Hashing>
    )

    /**
     * Rebuild the first spine of the [MerkleTree] with a new [primary].
     */
    fun buildFromPrimary(primary: Hashing)
}