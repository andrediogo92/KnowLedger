package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

fun MerkleTree.immutableCopy(): ImmutableMerkleTree =
    ImmutableMerkleTree(
        hashers = hashers, collapsedTree = collapsedTree,
        levelIndex = levelIndex
    )

@Suppress("NAME_SHADOWING")
internal fun loopUpVerification(
    index: Int, hash: Hash, level: Int,
    hasher: Hashers, collapsedTree: List<Hash>,
    levelIndex: List<Int>
): Boolean {
    var res: Boolean = hash == collapsedTree[index]
    var index = index
    var level = level
    var hash: Hash
    while (res && index != 0) {
        val delta = index - levelIndex[level]

        //Is a left leaf
        hash = if (delta % 2 == 0) {

            //Is an edge case left leaf
            if (index + 1 == collapsedTree.size ||
                (level + 1 != levelIndex.size &&
                        index + 1 == levelIndex[level + 1])
            ) {
                hasher.applyHash(
                    collapsedTree[index] + collapsedTree[index]
                )
            } else {
                hasher.applyHash(
                    collapsedTree[index] + collapsedTree[index + 1]
                )
            }
        }

        //Is a right leaf
        else {
            hasher.applyHash(
                collapsedTree[index - 1] + collapsedTree[index]
            )
        }
        level--

        //Index of parent is at the start of the last level
        // + the distance from start of this level / 2
        index = levelIndex[level] + (delta / 2)
        res = hash == collapsedTree[index]
    }
    return res
}

@Suppress("NAME_SHADOWING")
internal fun loopUpAllVerification(
    level: Int, hasher: Hashers,
    collapsedTree: List<Hash>, levelIndex: List<Int>
): Boolean {
    var res = true

    //2 fors evaluate to essentially checking level by level
    //starting at the second to last.
    //We already checked the last level immediately
    //against the values provided.
    var level = level
    while (level >= 0) {
        var i = levelIndex[level]
        while (i < levelIndex[level + 1]) {

            //Delta is level index difference + current index + difference
            //to current level index.
            //It checks out to exactly the left child leaf of any node.

            val delta = levelIndex[level + 1] - levelIndex[level] + i + (i - levelIndex[level])

            //Either the child is the last leaf in the next level, or is the last leaf.
            //Since we know delta points to left leafs both these conditions mean
            //edge case leafs.
            if ((level + 2 != levelIndex.size && delta + 1 == levelIndex[level + 2])
                || delta + 1 == collapsedTree.size
            ) {
                if (collapsedTree[i] != hasher.applyHash(
                        collapsedTree[delta] + collapsedTree[delta]
                    )
                ) {
                    res = false
                    break
                }
            }

            //Then it's a regular left leaf.
            else {
                if (collapsedTree[i] != hasher.applyHash(
                        collapsedTree[delta] + collapsedTree[delta + 1]
                    )
                ) {
                    res = false
                    break
                }
            }
            i += 1
        }
        level -= 1
    }
    return res
}


internal fun checkAllTransactionsPresent(
    start: Int, primary: Hashing, data: Array<out Hashing>,
    collapsedTree: List<Hash>
): Boolean =
    collapsedTree[start] == primary.hash && checkAllTransactionsPresent(
        start + 1, data, collapsedTree
    )


internal fun checkAllTransactionsPresent(
    start: Int, data: Array<out Hashing>, collapsedTree: List<Hash>
): Boolean {
    val hashed = data.map(Hashing::hash)
    var i = 0
    while (i < hashed.size && hashed[i] == collapsedTree[start + i]) i += 1
    return i == hashed.size
}
