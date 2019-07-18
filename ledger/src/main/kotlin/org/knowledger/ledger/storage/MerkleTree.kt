package org.knowledger.ledger.storage


import org.knowledger.common.Sizeable
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hash.Companion.emptyHash
import org.knowledger.common.hash.Hashed
import org.knowledger.common.hash.Hasher
import org.knowledger.common.storage.LedgerContract

interface MerkleTree : Sizeable, LedgerContract, Cloneable {
    val hasher: Hasher
    val nakedTree: List<Hash>
    val levelIndexes: List<Int>

    /**
     * The root hashId.
     */
    val root: Hash
        get() =
            if (nakedTree.isNotEmpty())
                nakedTree[0] else
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
        coinbase: Hashed,
        data: Array<out Hashed>
    ): Boolean


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
    fun rebuildMerkleTree(data: Array<out Hashed>)

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
        coinbase: Hashed, data: Array<out Hashed>
    )

}


