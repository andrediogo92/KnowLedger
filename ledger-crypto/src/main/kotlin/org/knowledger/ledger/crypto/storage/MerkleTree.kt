package org.knowledger.ledger.crypto.storage


import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.crypto.hash.Hashers

interface MerkleTree : HashSerializable,
                       Hashing,
                       LedgerContract,
                       Cloneable {
    val collapsedTree: List<Hash>
    val levelIndex: List<Int>
    val hasher: Hashers

    /**
     * The root hash.
     */
    override val hash: Hash
        get() =
            if (collapsedTree.isNotEmpty())
                collapsedTree[0] else
                emptyHash

    public override fun clone(): MerkleTree

    fun hasTransaction(hash: Hash): Boolean
    fun getTransactionId(hash: Hash): Int?

    /**
     * Takes a [hash] to verify against the [MerkleTree]
     * and returns whether the transaction is present and
     * matched all the way up the [MerkleTree].
     */
    fun verifyTransaction(hash: Hash): Boolean


    /**
     * Verifies entire [MerkleTree] against the transaction value.
     *
     * Takes the special [coinbase] transaction + the other [data]
     * transactions in the block and returns whether the entire
     * [MerkleTree] matches against the transaction [data] + [coinbase].
     */
    fun verifyBlockTransactions(
        coinbase: Hashing,
        data: Array<out Hashing>
    ): Boolean

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
     * Takes [data] as the transactions in the block + the special [coinbase] transaction's
     * hashes and outputs the full corresponding [Merkle Tree], or an empty [MerkleTree] if
     * supplied with empty [data].
     */
    fun rebuildMerkleTree(
        coinbase: Hashing, data: Array<out Hashing>
    )

    fun buildFromCoinbase(coinbase: Hashing)
}


