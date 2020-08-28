package org.knowledger.ledger.crypto.storage

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

fun MerkleTree.immutableCopy(): ImmutableMerkleTree =
    ImmutableMerkleTree(hashers, collapsedTree, levelIndex)

internal fun loopUpVerification(
    startIndex: Int, startHash: Hash, startLevel: Int, hashers: Hashers,
    collapsedTree: List<Hash>, levelIndex: List<Int>,
): Boolean {
    var hash: Hash = startHash
    var level = startLevel
    var delta = (startIndex - levelIndex[level])
    var index: Int = startIndex
    var res = hash == collapsedTree[index]
    //Test all parents upwards starting from supplied index.
    while (level > 0 && res) {
        //Calculate the parent hash for level - 1
        hash = calculateHashOfParentInLevel(delta, index, level, collapsedTree, levelIndex, hashers)
        level--
        //Difference to the start of the level for parent
        delta /= 2
        //Determine the index of the parent
        index = levelIndex[level] + delta

        //Determine current hash matches hash at index
        res = hash == collapsedTree[index]

    }
    return res
}

internal fun calculateHashOfParentInLevel(
    deltaFromLevel: Int, currentIndex: Int, currentLevel: Int,
    collapsedTree: List<Hash>, levelIndex: List<Int>, hashers: Hashers,
): Hash =
    //Is a left leaf
    if (deltaFromLevel % 2 == 0) {


        //Is an edge case left leaf
        if (currentIndex + 1 == collapsedTree.size ||
            (currentLevel + 1 != levelIndex.size &&
             currentIndex + 1 == levelIndex[currentLevel + 1])
        ) {
            hashers.applyHash(collapsedTree[currentIndex] + collapsedTree[currentIndex])
        } else {
            hashers.applyHash(collapsedTree[currentIndex] + collapsedTree[currentIndex + 1])
        }
    }

    //Is a right leaf
    else {
        hashers.applyHash(collapsedTree[currentIndex - 1] + collapsedTree[currentIndex])
    }

@Suppress("NAME_SHADOWING")
internal fun loopUpAllVerification(
    level: Int, hashers: Hashers, collapsedTree: List<Hash>, levelIndex: List<Int>,
): Boolean {
    var res = true

    //2 fors evaluate to essentially checking level by level
    //starting at the second to last.
    //We already checked the last level immediately
    //against the values provided.
    var hash: Hash
    var level = level
    while (level >= 0 && res) {
        var i = levelIndex[level]
        while (i < levelIndex[level + 1] && res) {

            //Delta is level index difference + current index + difference
            //to current level index.
            //It checks out to exactly the left child leaf of any node.
            val delta = levelIndex[level + 1] + (2 * i) - (2 * levelIndex[level])

            hash = calculateHashOfParentFromLeftLeaf(
                delta, level, collapsedTree, levelIndex, hashers
            )

            res = collapsedTree[i] == hash

            i += 1
        }
        level -= 1
    }
    return res
}

fun calculateHashOfParentFromLeftLeaf(
    deltaFromLevel: Int, level: Int, collapsedTree: List<Hash>,
    levelIndex: List<Int>, hashers: Hashers,
): Hash =
//Either the child is the last leaf in the next level,
//or is the last leaf of the tree in an uneven tree.
//Since we know delta points to left leafs both these conditions mean
    //edge case left leafs.
    if ((level + 2 < levelIndex.size && deltaFromLevel + 1 == levelIndex[level + 2])
        || deltaFromLevel + 1 == collapsedTree.size
    ) {
        hashers.applyHash(collapsedTree[deltaFromLevel] + collapsedTree[deltaFromLevel])
    }

    //Then it's a regular left leaf.
    else {
        hashers.applyHash(collapsedTree[deltaFromLevel] + collapsedTree[deltaFromLevel + 1])
    }


internal fun checkAllTransactionsPresent(
    start: Int, primary: Hashing, data: Array<out Hashing>, collapsedTree: List<Hash>,
): Boolean = collapsedTree[start] == primary.hash &&
             checkAllTransactionsPresent(start + 1, data, collapsedTree)


internal fun checkAllTransactionsPresent(
    start: Int, data: Array<out Hashing>, collapsedTree: List<Hash>,
): Boolean {
    val hashed = data.map(Hashing::hash)
    var i = 0
    while (i < hashed.size && hashed[i] == collapsedTree[start + i]) i += 1
    return i == hashed.size
}
