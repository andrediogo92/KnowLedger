package org.knowledger.ledger.crypto.storage

import org.knowledger.collections.fastPrefixAdd
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

data class MerkleTreeImpl(
    override val hashers: Hashers,
    internal val _collapsedTree: MutableList<Hash>,
    internal val _levelIndex: MutableList<Int>,
) : MutableMerkleTree {
    override val collapsedTree: List<Hash> get() = _collapsedTree
    override val levelIndex: List<Int> get() = _levelIndex


    override fun changeHasher(hasher: Hashers) {
        TODO("not implemented")
    }

    override fun applyMultiDiff(diff: Array<out Hashing>, diffIndexes: Array<Int>) {
        TODO("not implemented")
    }

    override fun fastAddSingle(element: Hashing, index: Int): Boolean =
        if (evenBase && index + levelIndex[levelIndex.size - 1] == collapsedTree.size) {
            _collapsedTree.add(element.hash)
            applySingleDiff(element, index)
            true
        } else {
            false
        }

    override fun applySingleDiff(diff: Hashing, diffIndex: Int) {
        var hash = diff.hash
        var level = levelIndex.size - 1
        var delta = diffIndex
        var index: Int = levelIndex[level] + delta
        _collapsedTree[index] = hash
        //Test all parents upwards starting from supplied index.
        while (level > 0) {
            //Calculate the parent hash for level - 1
            hash = calculateHashOfParentInLevel(delta, index, level, collapsedTree, levelIndex,
                                                hashers)
            level--
            //Difference to the start of the level for parent
            delta /= 2
            //Determine the index of the parent
            index = levelIndex[level] + delta

            //Determine current hash matches hash at index
            _collapsedTree[index] = hash
        }
    }

    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        _collapsedTree.clear()
        _levelIndex.clear()
        val treeLayers = mutableListOf<Array<Hash>>()
        treeLayers.add(data.mapToArray(Hashing::hash))
        buildLoop(treeLayers)
    }

    override fun rebuildMerkleTree(primary: Hashing, data: Array<out Hashing>) {
        rebuildMerkleTree(data.fastPrefixAdd(primary))
    }

    private fun buildLoop(treeLayers: MutableList<Array<Hash>>) {
        var i = 0
        //Next layer's node count for depth-1
        var count = (treeLayers[i].size / 2) + (treeLayers[i].size % 2)
        //While we're not at root yet:
        while (count > 1) {
            treeLayers.add(buildNewLayer(treeLayers[i], count))
            //Update to new count in next tree layer.
            count = (count / 2) + (count % 2)
            i += 1
        }
        when (treeLayers[i].size) {
            2 -> {
                val left = treeLayers[i][0]
                val right = treeLayers[i][1]
                _collapsedTree.add(
                    hashers.applyHash(left + right)
                )
                _levelIndex.add(0)
                count = 1
                for (s in treeLayers.asReversed()) {
                    _levelIndex.add(count)
                    _collapsedTree.addAll(s)
                    count += s.size
                }

            }
            //If the previous layer was already length 1,
            //that means we started at the root.
            1 -> {
                val root = treeLayers[i][0]
                _collapsedTree.add(hashers.applyHash(root + root))
                _levelIndex.add(0)
                _collapsedTree.add(root)
                _levelIndex.add(1)
            }
            //No data or primary were passed. Purely empty tree.
            0 -> {
                _collapsedTree.add(Hash.emptyHash)
                _levelIndex.add(0)
            }
            else -> {
                throw InvalidMerkleException(treeLayers[i].size)
            }
        }
    }

    class InvalidMerkleException(val size: Int) : Throwable() {
        override val message: String?
            get() = "${super.message}: penultimate level was $size in size."
    }

    private fun buildNewLayer(previousTreeLayer: Array<Hash>, count: Int): Array<Hash> {
        val treeLayer = Array(count) { Hash.emptyHash }
        var j = 0
        var i = 1
        //While we're inside the bounds of this layer, calculate two by two the hashId.
        while (i < previousTreeLayer.size) {
            treeLayer[j] = hashers.applyHash(previousTreeLayer[i - 1] + previousTreeLayer[i])
            i += 2
            j += 1
        }
        //If we're still in the layer, there's one left, it's grouped and hashed with itself.
        if (j < treeLayer.size) {
            treeLayer[j] = hashers.applyHash(previousTreeLayer[i - 1] + previousTreeLayer[i - 1])
        }
        return treeLayer
    }

    override fun buildFromPrimary(primary: Hashing) {
        applySingleDiff(primary, 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MerkleTree) return false
        if (collapsedTree != other.collapsedTree) return false
        if (levelIndex != other.levelIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hashers.hashCode()
        result = 31 * result + collapsedTree.hashCode()
        result = 31 * result + levelIndex.hashCode()
        return result
    }
}