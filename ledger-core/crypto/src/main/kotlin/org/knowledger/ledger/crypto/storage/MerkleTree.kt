package org.knowledger.ledger.crypto.storage


import org.knowledger.collections.fastSlice
import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

interface MerkleTree : Hashing,
                       LedgerContract {
    val collapsedTree: List<Hash>
    val levelIndex: List<Int>
    val hasher: Hashers

    /**
     * The root hash, defined as the first hash of the [collapsedTree].
     */
    override val hash: Hash
        get() = collapsedTree[0]


    fun hasTransaction(hash: Hash): Boolean {
        //Cache lists in case they are synthetic properties
        val cT = collapsedTree
        val lI = levelIndex

        //levelIndex[index] points to leftmost node at level index of the tree.
        val leafsIndex = lI[(lI.size - 1)]
        return cT.fastSlice(leafsIndex, cT.size).contains(hash)
    }

    fun getTransactionId(hash: Hash): Int {
        //Cache lists in case they are synthetic properties
        val cT = collapsedTree
        val lI = levelIndex

        //levelIndex[index] points to leftmost node at level index of the tree.
        val leafsIndex = lI[(lI.size - 1)]
        return cT.fastSlice(leafsIndex, cT.size).indexOf(hash) + leafsIndex
    }


    /**
     * Takes a [hash] to verify against the [MerkleTree]
     * and returns whether the transaction is present and
     * matched all the way up the [MerkleTree].
     */
    fun verifyTransaction(hash: Hash): Boolean {
        //Cache list in case they are synthetic properties
        val lI = levelIndex
        return getTransactionId(hash).let { i ->
            if (i != -1) {
                loopUpVerification(
                    i, hash, lI.size - 1,
                    hasher, collapsedTree, lI
                )
            } else false
        }
    }


    /**
     * Verifies entire [MerkleTree] against the transaction value.
     *
     * Takes the [data] transactions in the block and returns whether
     * the entire [MerkleTree] matches against the transaction [data].
     */
    fun verifyBlockTransactions(
        data: Array<out Hashing>
    ): Boolean {
        //Cache lists in case they are synthetic properties
        val cT = collapsedTree
        val lI = levelIndex
        val tStart = lI[lI.size - 1]
        //Check if collapsedTree is empty.
        //Check last level is the same size as data.
        if (cT.isNotEmpty() && cT.size - tStart == data.size) {
            //Check all transactions in last level match provided.
            if (checkAllTransactionsPresent(tStart, data, cT)) {
                return if (lI.size > 1) {
                    //Verify all transactions created upwards.
                    loopUpAllVerification(lI.size - 2, hasher, cT, lI)
                } else {
                    true
                }
            }
        }
        return false
    }

    /**
     * Verifies entire [MerkleTree] against the transaction value.
     *
     * Takes the special [primary] transaction + the other [data]
     * transactions in the block and returns whether the entire
     * [MerkleTree] matches against the transaction [data] + [primary].
     */
    fun verifyBlockTransactions(
        primary: Hashing, data: Array<out Hashing>
    ): Boolean {
        //Cache lists in case they are synthetic properties
        val cT = collapsedTree
        val lI = levelIndex
        val tStart = lI[lI.size - 1]
        //Check if collapsedTree is empty.
        //Check last level is the same size has primary + data.
        if (cT.isNotEmpty() && cT.size - tStart == data.size + 1) {
            //Check all transactions in last level match provided.
            if (checkAllTransactionsPresent(tStart, primary, data, cT)) {
                return if (lI.size > 1) {
                    //Verify all transactions created upwards.
                    loopUpAllVerification(lI.size - 2, hasher, cT, lI)
                } else {
                    true
                }
            }
        }
        return false
    }
}


