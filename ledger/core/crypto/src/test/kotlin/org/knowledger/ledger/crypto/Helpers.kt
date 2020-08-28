package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.fastPrefixAdd
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.testing.core.applyHashInPairs
import org.tinylog.kotlin.Logger
import kotlin.math.min

private inline fun <reified T> Array<T>.padDiff(size: Int): Array<T> {
    if (this.size == size) {
        return this
    }
    return Array(size) { this[it % this.size] }
}

/**
 * Equivalent in balanced trees to:
 *
 *      val treeClone = merkleTreeFrom(primary7, ts7)
 *
 * Two levels in to the left (index 3) is a hash of transaction 1 + 2.
 *
 *      assertThat(treeClone[3].bytes).containsExactly(
 *          *hashers.applyHash(coinbase7.hash + ts7[0].hash).bytes
 *      assertThat(treeClone[4].bytes).containsExactly(
 *          *hashers.applyHash(ts7[1].hash + ts7[2].hash).bytes
 *      )
 *      assertThat(treeClone[5].bytes).containsExactly(
 *          *hashers.applyHash(ts7[3].hash + ts7[4].hash).bytes
 *      )
 *      assertThat(treeClone[6].bytes).containsExactly(
 *          *hashers.applyHash(ts7[5].hash + ts7[6].hash).bytes
 *      )
 *
 * One level to the left (index 1) is a hash of the hash of
 * transactions 1 + 2 and 3 + 4
 *
 *      assertThat(treeClone[1].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  primary7.hash, ts7[0].hash,
 *                  ts7[1].hash, ts7[2].hash
 *              )
 *          ).bytes
 *      )
 *
 * One level to the right (index 2) is a hash of the hash of
 * transactions 5 + 6 and 7 + 8
 *
 *      assertThat(treeClone[2].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  ts7[3].hash, ts7[4].hash,
 *                  ts7[5].hash, ts7[6].hash
 *              )
 *          ).bytes
 *     )
 *
 * Root is hash of everything.
 *
 *      assertThat(treeClone[0].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  primary7.hash, ts7[0].hash,
 *                  ts7[1].hash, ts7[2].hash,
 *                  ts7[3].hash, ts7[4].hash,
 *                  ts7[5].hash, ts7[6].hash
 *              )
 *          ).bytes
 *      )
 *
 *
 * Equivalent in unbalanced trees to:
 *
 *      val nakedTree = merkleTreeFrom(ts6)
 *
 * Two levels in to the left (index 3) is a hash of transaction 1 + 2.
 *
 *      assertThat(nakedTree[3].bytes).containsExactly(
 *          *hashers.applyHash(ts6[0].hash + ts6[1].hash).bytes
 *      assertThat(nakedTree[4].bytes).containsExactly(
 *          *hashers.applyHash(ts6[2].hash + ts6[3].hash).bytes
 *      )
 *      assertThat(nakedTree[5].bytes).containsExactly(
 *          *hashers.applyHash(ts6[4].hash + ts6[5].hash).bytes
 *      )
 *
 * One level to the left (index 1) is a hash of the hash of
 * transactions 1 + 2 and 3 + 4
 *
 *      assertThat(nakedTree[1].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  ts6[0].hash, ts6[1].hash,
 *                  ts6[3].hash, ts6[4].hash
 *              )
 *          ).bytes
 *      )
 *
 * One level to the right (index 2) is a hash of the hash of
 * transactions 5 + 6 * 2
 *
 *      assertThat(nakedTree[2].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  ts6[5].hash, ts6[6].hash,
 *                  ts6[5].hash, ts6[6].hash
 *              )
 *          ).bytes
 *     )
 *
 * Root is hash of everything.
 *
 *      assertThat(nakedTree[0].bytes).containsExactly(
 *          *applyHashInPairs(
 *              hashers, arrayOf(
 *                  ts6[0].hash, ts6[1].hash,
 *                  ts6[2].hash, ts6[3].hash,
 *                  ts6[4].hash, ts6[5].hash
 *              )
 *          ).bytes
 *      )
 *
 */
fun assertUpstream(subslice: Array<Hash>, transactions: Array<out Hashing>, hashers: Hashers) {
    @Suppress("NAME_SHADOWING")
    val transactions = transactions.mapToArray(Hashing::hash)
    //Start calculating pairs for the entire range of transactions.
    //This will be equal to Tree root.
    var size = transactions.size
    var nextIndexToReplace = 1
    var levelIndex = 0
    subslice.forEachIndexed { index, hash ->
        //Range of transactions shrinks each level.
        //TxIndex keeps track of where we are at in a tree level.
        if (index >= nextIndexToReplace) {
            size /= 2
            size += size % 2
            nextIndexToReplace *= 2 + 1
            levelIndex = 0
        }
        //When a level is too small and leaves an unbalanced tree,
        // must choose to clamp and pad until we have enough transactions
        //to pair.
        val upper = min(transactions.size, (levelIndex + 1) * size)
        val slice = transactions
            .sliceArray(levelIndex * size until upper)
            .padDiff(size)
        //Apply an hash from a range of transactions. For 8 transactions:
        //[tx1, tx2, tx3, tx4] -> left branch 2 levels from leaves and 2 levels from root.
        //[tx1, tx2] -> left branch 1 level from leaves, 3 levels from the root.
        val hashed = applyHashInPairs(hashers, slice)
        assertThat(hashed.bytes).containsExactly(*hash.bytes)
        levelIndex++
    }
}

fun assertUpstream(
    subslice: Array<Hash>, primary: Hashing, transactions: Array<out Hashing>, hashers: Hashers,
) {
    assertUpstream(subslice, transactions.fastPrefixAdd(primary), hashers)
}

fun assertPrimaryRehash(indexes: Array<Int>, treeClone: Array<Hash>, tree: MerkleTree) {
    indexes.forEach { index ->
        assertThat(treeClone[index]).isNotEqualTo(tree.collapsedTree[index])
    }
}

fun assertEqualSubslice(subslice: Array<Hash>, transactions: Array<out Hashing>) {
    subslice.forEachIndexed { index, hash ->
        assertThat(hash.bytes).containsExactly(*transactions[index].hash.bytes)
    }
}

fun assertEqualSubsliceWithPrimary(
    subslice: Array<Hash>, primary: Hashing, transactions: Array<out Hashing>,
) {
    assertEqualSubslice(subslice, transactions.fastPrefixAdd(primary))
}

fun assertVerificationIndividually(merkleTree: MerkleTree, transactions: Array<out Hashing>) {
    assertThat(merkleTree.hash).isNotNull()
    transactions.forEach {
        assertThat(merkleTree.verifyTransaction(it.hash)).isTrue()
    }
}

fun assertVerificationIndividually(
    merkleTree: MerkleTree, primary: Hashing, transactions: Array<out Hashing>,
) {
    assertVerificationIndividually(merkleTree, transactions.fastPrefixAdd(primary))
}


fun logMerkle(ts: Array<out Hashing>, tree: MerkleTree) {
    val builder = StringBuilder(tree.collapsedTree.size * 2 * Hash.TRUNC)
    tree.collapsedTree.forEachIndexed { i, it ->
        builder.append("Naked tree @").append(i).append(" -> ").appendLine(it.base64Encoded())
    }
    Logger.debug(builder::toString)

    builder.setLength(0)
    tree.levelIndex.forEachIndexed { i, it ->
        builder.append("Level @").append(i).append(" -> Starts from ").appendLine(it)
    }
    Logger.debug(builder::toString)

    builder.setLength(0)
    ts.forEachIndexed { i, it ->
        builder.append("Transactions @").append(i).append(" -> ")
            .appendLine(it.hash.base64Encoded())
    }
    Logger.debug(builder::toString)
}

fun logMerkle(primary: Hashing, ts: Array<out Hashing>, tree: MerkleTree) {
    logMerkle(ts, tree)
    Logger.debug { "Primary is ${primary.hash.base64Encoded()}" }
}

