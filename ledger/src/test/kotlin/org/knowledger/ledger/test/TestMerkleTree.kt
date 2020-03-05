package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.testing.core.applyHashInPairs
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.testHasher
import org.tinylog.kotlin.Logger
import kotlin.math.min

/**
 * TODO: Move low-level testing to crypto module.
 */
class TestMerkleTree {
    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    private val size = 24
    private val base = generateXTransactionsArray(id, size)
    private var begin = random.randomInt(size - 7)
    private val ts7 = base.sliceArray(begin until begin + 7)

    //Cache coinbase params to avoid repeated digest of formula calculations.
    private val coinbaseParams = CoinbaseParams()

    private inline fun <reified T> Array<T>.padDiff(size: Int): Array<T> {
        if (this.size == size) {
            return this
        }
        return Array(size) {
            this[it % this.size]
        }
    }

    /**
     * Equivalent in balanced trees to:
     *
     *      val treeClone = merkleTreeFrom(coinbase7, ts7)
     *
     * Two levels in to the left (index 3) is a hash of transaction 1 + 2.
     *
     *      assertThat(treeClone[3].bytes).containsExactly(
     *          *testHasher.applyHash(coinbase7.hash + ts7[0].hash).bytes
     *      assertThat(treeClone[4].bytes).containsExactly(
     *          *testHasher.applyHash(ts7[1].hash + ts7[2].hash).bytes
     *      )
     *      assertThat(treeClone[5].bytes).containsExactly(
     *          *testHasher.applyHash(ts7[3].hash + ts7[4].hash).bytes
     *      )
     *      assertThat(treeClone[6].bytes).containsExactly(
     *          *testHasher.applyHash(ts7[5].hash + ts7[6].hash).bytes
     *      )
     *
     * One level to the left (index 1) is a hash of the hash of
     * transactions 1 + 2 and 3 + 4
     *
     *      assertThat(treeClone[1].bytes).containsExactly(
     *          *applyHashInPairs(
     *              testHasher, arrayOf(
     *                  coinbase7.hash, ts7[0].hash,
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
     *              testHasher, arrayOf(
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
     *              testHasher, arrayOf(
     *                  coinbase7.hash, ts7[0].hash,
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
     *          *testHasher.applyHash(ts6[0].hash + ts6[1].hash).bytes
     *      assertThat(nakedTree[4].bytes).containsExactly(
     *          *testHasher.applyHash(ts6[2].hash + ts6[3].hash).bytes
     *      )
     *      assertThat(nakedTree[5].bytes).containsExactly(
     *          *testHasher.applyHash(ts6[4].hash + ts6[5].hash).bytes
     *      )
     *
     * One level to the left (index 1) is a hash of the hash of
     * transactions 1 + 2 and 3 + 4
     *
     *      assertThat(nakedTree[1].bytes).containsExactly(
     *          *applyHashInPairs(
     *              testHasher, arrayOf(
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
     *              testHasher, arrayOf(
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
     *              testHasher, arrayOf(
     *                  ts6[0].hash, ts6[1].hash,
     *                  ts6[2].hash, ts6[3].hash,
     *                  ts6[4].hash, ts6[5].hash
     *              )
     *          ).bytes
     *      )
     *
     */
    private fun assertUpstream(
        subslice: Array<Hash>,
        transactions: Array<out Hashing>
    ) {
        @Suppress("NAME_SHADOWING")
        val transactions = transactions.mapToArray { it.hash }
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
            val slice = transactions.sliceArray(
                levelIndex * size until upper
            ).padDiff(size)
            //Apply an hash from a range of transactions. For 8 transactions:
            //[tx1, tx2, tx3, tx4] -> left branch 2 levels from leaves and 2 levels from root.
            //[tx1, tx2] -> left branch 1 level from leaves, 3 levels from the root.
            val hashed = applyHashInPairs(
                testHasher, slice
            )
            assertThat(hashed.bytes).containsExactly(*hash.bytes)
            levelIndex++
        }
    }

    private fun assertUpstream(
        subslice: Array<Hash>, coinbase: Hashing,
        transactions: Array<out Hashing>
    ) {
        assertUpstream(subslice, arrayOf(coinbase, *transactions))
    }

    private fun assertCoinbaseRehash(
        indexes: Array<Int>, treeClone: Array<Hash>,
        tree: MerkleTree
    ) {
        for (index in indexes) {
            assertThat(treeClone[index]).isNotEqualTo(tree.collapsedTree[index])
        }
    }

    private fun assertEqualSubslice(
        subslice: Array<Hash>, transactions: Array<out Hashing>
    ) {
        subslice.forEachIndexed { index, hash ->
            assertThat(hash.bytes).containsExactly(*transactions[index].hash.bytes)
        }
    }

    private fun assertEqualSubsliceCoinbase(
        subslice: Array<Hash>,
        coinbase: Hashing, transactions: Array<out Hashing>
    ) {
        assertEqualSubslice(subslice, arrayOf(coinbase, *transactions))
    }

    private fun assertVerificationIndividually(
        merkleTree: MerkleTree, transactions: Array<out Hashing>
    ) {
        assertThat(merkleTree.hash).isNotNull()
        transactions.forEach {
            assertThat(
                merkleTree.verifyTransaction(it.hash)
            ).isTrue()
        }
    }

    private fun assertVerificationIndividually(
        merkleTree: MerkleTree,
        coinbase: Hashing, transactions: Array<out Hashing>
    ) {
        assertVerificationIndividually(
            merkleTree, arrayOf(coinbase, *transactions)
        )
    }


    private fun logMerkle(
        ts: Array<Transaction>,
        tree: MerkleTree
    ) {
        val builder = StringBuilder(
            tree.collapsedTree.size * 2 * Hash.TRUNC
        )
        tree.collapsedTree.forEachIndexed { i, it ->
            builder.append("Naked tree @").append(i).append(" -> ")
                .appendln(it.base64Encoded())
        }
        Logger.debug { builder.toString() }

        builder.setLength(0)
        tree.levelIndex.forEachIndexed { i, it ->
            builder.append("Level @").append(i)
                .append(" -> Starts from ").appendln(it)
        }
        Logger.debug { builder.toString() }

        builder.setLength(0)
        ts.forEachIndexed { i, it ->
            builder.append("Transactions @").append(i).append(" -> ")
                .appendln(it.hash.base64Encoded())
        }
        Logger.debug { builder.toString() }
    }

    private fun logMerkle(
        coinbase: Coinbase,
        ts: Array<Transaction>,
        tree: MerkleTree
    ) {
        logMerkle(ts, tree)
        Logger.debug {
            "Coinbase is ${coinbase.hash.base64Encoded()}"
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        init {
            begin = random.randomInt(size - 8)
        }

        val coinbase7 = generateCoinbase(
            ts = ts7, coinbaseParams = coinbaseParams
        )
        val tree7WithCoinbase =
            MerkleTreeImpl(testHasher, coinbase7, ts7)
        private val ts8 = base.sliceArray(begin until begin + 8)
        private val tree8 = MerkleTreeImpl(testHasher, ts8)

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts8, tree8)

            //Root is present
            assertThat(tree8.hash).isNotNull()
            val nakedTree = tree8.collapsedTree.toTypedArray()
            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubslice(nakedTree.sliceArray(7..14), ts8)
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts8, tree8)
            assertVerificationIndividually(tree8, ts8)
            Logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }

        @Test
        fun `merkle tree coinbase recreation`() {
            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)

            val treeClone = tree7WithCoinbase.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(0..6), coinbase7, ts7)


            //Alter coinbase to add extra witness.
            coinbase7.addToWitness(
                coinbase7.witnesses[0], 4, ts7[4]
            )

            //Recalculate only coinbase propagation
            tree7WithCoinbase.buildFromCoinbase(coinbase7)

            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)

            //Root is present
            assertThat(tree7WithCoinbase.hash).isNotNull()
            val nakedTree = tree7WithCoinbase.collapsedTree.toTypedArray()

            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubsliceCoinbase(nakedTree.sliceArray(7..14), coinbase7, ts7)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..6), coinbase7, ts7)

            //Verify coinbase update triggered left branch rehash up to root.
            assertCoinbaseRehash(
                arrayOf(0, 1, 3, 7), treeClone,
                tree7WithCoinbase
            )
        }

        @Test
        fun `all transaction verification`() {
            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)
            assertThat(tree7WithCoinbase.hash).isNotNull()
            assertThat(
                tree7WithCoinbase.verifyBlockTransactions(coinbase7, ts7)
            ).isTrue()
            Logger.debug {
                "Balanced 7-transaction tree with payout is correct"
            }
        }


    }

    @Nested
    inner class UnbalancedMerkleTree {
        init {
            begin = random.randomInt(size - 5)
        }

        private val ts5 = base.sliceArray(begin until begin + 5)

        init {
            begin = random.randomInt(size - 6)
        }

        private val ts6 = base.sliceArray(begin until begin + 6)
        private val coinbase5 = generateCoinbase(
            ts = ts5, coinbaseParams = coinbaseParams
        )
        private val tree5WithCoinbase =
            MerkleTreeImpl(testHasher, coinbase5, ts5)
        private val tree6 = MerkleTreeImpl(testHasher, ts6)


        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts6, tree6)

            //Root is present
            assertThat(tree6.hash).isNotNull()
            val nakedTree = tree6.collapsedTree.toTypedArray()

            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubslice(nakedTree.sliceArray(6..11), ts6)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..5), ts6)
        }

        @Test
        fun `merkle tree coinbase recreation`() {
            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            val treeClone = tree5WithCoinbase.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(0..5), coinbase5, ts5)


            //Alter coinbase to add extra witness.
            coinbase5.addToWitness(
                coinbase5.witnesses[0], 4, ts5[4]
            )

            //Recalculate only coinbase propagation
            tree5WithCoinbase.buildFromCoinbase(coinbase5)

            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            //Root is present
            assertThat(tree5WithCoinbase.hash).isNotNull()
            val nakedTree = tree5WithCoinbase.collapsedTree.toTypedArray()

            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubsliceCoinbase(nakedTree.sliceArray(6..11), coinbase5, ts5)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..5), coinbase5, ts5)

            //Verify coinbase update triggered left branch rehash up to root.
            assertCoinbaseRehash(
                arrayOf(0, 1, 3, 6), treeClone,
                tree5WithCoinbase
            )
        }

        @Test
        fun `merkle tree creation with coinbase`() {

            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)


            //Root is present
            assertThat(tree5WithCoinbase.hash).isNotNull()
            val nakedTree = tree5WithCoinbase.collapsedTree.toTypedArray()

            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubsliceCoinbase(nakedTree.sliceArray(6..11), coinbase5, ts5)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..5), coinbase5, ts5)
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts5, tree5WithCoinbase)
            assertVerificationIndividually(tree5WithCoinbase, coinbase5, ts5)
            Logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }

            val tree7 = MerkleTreeImpl(testHasher, ts7)

            //Log constructed merkle
            logMerkle(ts7, tree7)
            assertVerificationIndividually(tree7, ts7)
            Logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {

            val coinbase6 = generateCoinbase(
                ts = ts6, coinbaseParams = coinbaseParams
            )
            val tree6WithCoinbase =
                MerkleTreeImpl(testHasher, coinbase6, ts6)

            //Log constructed merkle
            logMerkle(coinbase6, ts6, tree6WithCoinbase)

            assertThat(tree6WithCoinbase.hash).isNotNull()
            assertThat(
                tree6WithCoinbase.verifyBlockTransactions(coinbase6, ts6)
            ).isTrue()

            Logger.debug {
                "Unbalanced 6-transaction tree with payout is correct"
            }


            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.hash).isNotNull()
            assertThat(
                tree5WithCoinbase.verifyBlockTransactions(coinbase5, ts5)
            ).isTrue()

            Logger.debug {
                "Unbalanced 5-transaction tree with payout is correct"
            }

        }

    }

    @Test
    fun `merkle tree creation with just root`() {
        begin = random.randomInt(size - 1)

        val ts = arrayOf(base[begin])
        val tree = MerkleTreeImpl(testHasher, ts)

        //Log constructed merkle
        logMerkle(ts, tree)

        assertThat(tree.hash).isNotNull()
        //Root matches the only transaction.
        assertThat(tree.hash.bytes).containsExactly(*ts[0].hash.bytes)
        assertThat(tree.collapsedTree.size).isEqualTo(1)
    }
}
