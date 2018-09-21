package pt.um.lei.masb.blockchain.data


import mu.KLogging
import pt.um.lei.masb.blockchain.Hashed
import pt.um.lei.masb.blockchain.Sizeable
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER

class MerkleTree(
        val collapsedTree: List<String> = emptyList(),
        val levelIndex: List<Int> = emptyList()
) : Sizeable {
    companion object : KLogging() {

        /**
         * Build a merkle tree collapsed in a heap for easy navigability from bottom up.
         * <pw>
         * Initializes the first tree layer, which is the transaction layer.
         * <pw>
         * Sets a correspondence from each hash to it's index.
         * @param data  Transactions in the block.
         * @return The corresponding MerkleTree or empty MerkleTree if empty transactions.
         */
        fun buildMerkleTree(data: List<Hashed>): MerkleTree {
            val treeLayer = mutableListOf<Array<String>>()
            treeLayer.add(data.map(Hashed::hashId).toTypedArray())
            return buildLoop(treeLayer)
        }

        /**
         * Build a merkle tree collapsed in a heap for easy navigability from bottom up.
         * <pw>
         * Convenience method for pre-pending coinbase as first element in bottom layer.
         * <pw>
         * Initialize the first tree layer, which is the transaction layer.
         * <pw>
         * Sets a correspondence from each hash to it's index.
         *
         * @param coinbase Coinbase of the block.
         * @param data     Transactions in the block.
         * @return The corresponding MerkleTree or empty MerkleTree if empty transactions.
         */
        fun buildMerkleTree(coinbase: Hashed, data: List<Hashed>): MerkleTree {
            val treeLayer = mutableListOf<Array<String>>()
            treeLayer.add(arrayOf(coinbase.hashId, *data.map(Hashed::hashId).toTypedArray()))
            return buildLoop(treeLayer)
        }

        /**
         * Build loop that builds a Merkle Tree collapsed in a heap.
         * <pw>
         * Start at leaves and iteratively builds the next layer at
         * depth-1 until it arrives at root.
         * <pw>
         *
         * @param treeLayer A container for the successive layers, containing layer = depth.
         * @return The completed merkle tree.
         */
        private fun buildLoop(treeLayer: MutableList<Array<String>>): MerkleTree {
            var i = 0
            //Next layer's node count for depth-1
            var count = (treeLayer[i].size / 2) + (treeLayer[i].size % 2)
            //While we're not at root yet:
            while (count > 1) {
                treeLayer.add(buildNewLayer(treeLayer[i], count))
                //Update to new count in next tree layer.
                count = (count / 2) + (count % 2)
                i += 1
            }
            return when {
                treeLayer[i].size == 2 -> {
                    val tempTree = mutableListOf<String>()
                    val tempIndex = mutableListOf<Int>()
                    tempTree.add(DEFAULT_CRYPTER.applyHash(treeLayer[i][0] + treeLayer[i][1]))
                    tempIndex.add(0)
                    treeLayer.reverse()
                    count = 1
                    for (s in treeLayer) {
                        tempIndex.add(count)
                        tempTree.addAll(s)
                        count += s.size
                    }
                    MerkleTree(tempTree.toList(), tempIndex.toList())
                }
                //If the previous layer was already length 1, that means we started at the root.
                treeLayer[i].size == 1 -> {
                    MerkleTree(listOf(treeLayer[i][0]))
                }
                else -> {
                    logger.warn { "Empty merkle tree" }
                    MerkleTree()
                }
            }
        }

        /**
         * Build the next tree layer. Sets proper descendents and ascendents.
         * <pw>
         * Checks for oddness, and if odd length create a single left child for remainder.
         *
         * @param previousTreeLayer The previous tree layer, depth+1
         * @param count             The node count for this next layer.
         * @return The new Layer of hashes of their children set.
         */
        fun buildNewLayer(previousTreeLayer: Array<String>,
                          count: Int): Array<String> {
            val treeLayer = Array(count) { "" }
            var j = 0
            var i = 1
            //While we're inside the bounds of this layer, calculate two by two the hash.
            while (i < previousTreeLayer.size) {
                treeLayer[j] = DEFAULT_CRYPTER.applyHash(previousTreeLayer[i - 1] +
                                                         previousTreeLayer[i])
                i += 2
                j += 1
            }
            //If we're still in the layer, there's one left, it's grouped and hashed with itself.
            if (j < treeLayer.size) {
                treeLayer[j] = DEFAULT_CRYPTER.applyHash(previousTreeLayer[previousTreeLayer.size - 1] +
                                                         previousTreeLayer[previousTreeLayer.size - 1])
            }
            return treeLayer
        }


    }


    /**
     * The root hash.
     */
    val root: String get() = collapsedTree[0]


    fun hasTransaction(hash: String): Boolean {
        var res = false
        var i = collapsedTree.size - 1

        //levelIndex[index] points to leftmost node at level index of the tree
        while (i >= levelIndex[(levelIndex.size - 1)]) {
            if (collapsedTree[i] == hash) {
                res = true
                break
            }
            i -= 1
        }
        return res
    }

    fun getTransactionId(hash: String): Int? {
        var res: Int? = null
        var i = collapsedTree.size - 1

        //levelIndex[index] points to leftmost node at level index of the tree.
        while (i >= levelIndex[levelIndex.size - 1]) {
            if (collapsedTree[i] == hash) {
                res = i
                break
            }
            i -= 1
        }
        return res
    }


    /**
     * @param hash  hash to verify against merkleTree.
     * @return Whether the transaction is present and
     *              matched all the way up the merkleTree.
     */
    fun verifyTransaction(hash: String): Boolean =
            getTransactionId(hash)?.let {
                loopUpVerification(it, hash, levelIndex.size - 1)
            } ?: false


    private fun loopUpVerification(index: Int, hash: String, level: Int): Boolean {
        var res: Boolean = hash == (collapsedTree[index])
        var index = index
        var level = level
        var hash: String
        while (res && index != 0) {
            val delta = index - levelIndex[level]

            //Is a left leaf
            hash = if (delta % 2 == 0) {

                //Is an edge case left leaf
                if (index + 1 == collapsedTree.size ||
                    (level + 1 != levelIndex.size &&
                     index + 1 == levelIndex[level + 1])) {
                    DEFAULT_CRYPTER.applyHash(collapsedTree[index] + collapsedTree[index])
                } else {
                    DEFAULT_CRYPTER.applyHash(collapsedTree[index] + collapsedTree[index + 1])
                }
            }

            //Is a right leaf
            else {
                DEFAULT_CRYPTER.applyHash(collapsedTree[index - 1] + collapsedTree[index])
            }
            level--

            //Index of parent is at the start of the last level
            // + the distance from start of this level / 2
            index = levelIndex[level] + (delta / 2)
            res = hash == (collapsedTree[index])
        }
        return res
    }

    /**
     * Verifies entire merkleTree against the transaction data.
     *
     * @param coinbase The coinbase transaction.
     * @param data The transaction data.
     * @return Whether the entire merkleTree matches
     * against the transaction data.
     */
    fun verifyBlockTransactions(coinbase: Hashed,
                                data: List<Hashed>): Boolean =
            if (checkAllTransactionsPresent(coinbase, data)) {
                if (collapsedTree.size != 1) {
                    loopUpAllVerification(levelIndex.size - 2)
                } else {
                    true
                }
            } else {
                false
            }


    private fun loopUpAllVerification(level: Int): Boolean {
        var res = true

        //2 fors evaluate to essentially checking level by level
        //starting at the second to last.
        //We already checked the last level immediately
        //against the data provided.
        var level = level
        while (level >= 0) {
            var i = levelIndex[level]
            while (i < levelIndex[level + 1]) {

                //Delta is level index difference + current index + difference
                //to current level index.
                //It checks out to exactly the left child leaf of any node.

                val delta = levelIndex[level + 1] -
                            levelIndex[level] +
                            i +
                            (i - levelIndex[level])

                //Either the child is the last leaf in the next level, or is the last leaf.
                //Since we know delta points to left leafs both these conditions mean
                //edge case leafs.
                if ((level + 2 != levelIndex.size && delta + 1 == levelIndex[level + 2])
                    || delta + 1 == collapsedTree.size) {
                    if (collapsedTree[i] !=
                        DEFAULT_CRYPTER.applyHash(collapsedTree[delta] +
                                                  collapsedTree[delta])) {
                        res = false
                        break
                    }
                }

                //Then it's a regular left leaf.
                else {
                    if (collapsedTree[i] !=
                        DEFAULT_CRYPTER.applyHash(collapsedTree[delta] +
                                                  collapsedTree[delta + 1])) {
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

    private fun checkAllTransactionsPresent(coinbase: Hashed,
                                            data: List<Hashed>): Boolean {
        var i = levelIndex[levelIndex.size - 1] + 1
        var res = true
        val arr = data.map(Hashed::hashId)
        if (collapsedTree[i - 1] == coinbase.hashId) {
            for (it in arr) {

                //There are at least as many transactions.
                //They match the ones in the merkle tree.
                if (i < collapsedTree.size && it == collapsedTree[i]) {
                    i++
                } else {
                    res = false
                    break
                }
            }

            //There are less transactions in the provided block
            if (i != collapsedTree.size) {
                res = false
            }
        } else {
            res = false
        }
        return res
    }


}


