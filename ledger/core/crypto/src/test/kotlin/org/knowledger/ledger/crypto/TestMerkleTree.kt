package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.fastPrefixAdd
import org.knowledger.collections.mapToArray
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.crypto.storage.MerkleTreeFactoryImpl
import org.knowledger.testing.core.applyHashInPairs
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.knowledger.testing.core.randomHash
import org.tinylog.kotlin.Logger
import kotlin.math.min

/**
 *
 */
class TestMerkleTree {
    private val hashers: Hashers = defaultHasher
    private val factory: MerkleTreeFactory = MerkleTreeFactoryImpl()

    class EmptyHashing : Hashing {
        override val hash: Hash = randomHash()
    }

    private val size = 24
    private val base = Array<Hashing>(size) { EmptyHashing() }
    private val begin7 = random.randomInt(size - 7)
    private val ts7 = base.sliceArray(begin7 until begin7 + 7)

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
                hashers, slice
            )
            assertThat(hashed.bytes).containsExactly(*hash.bytes)
            levelIndex++
        }
    }

    private fun assertUpstream(
        subslice: Array<Hash>, primary: Hashing,
        transactions: Array<out Hashing>
    ) {
        assertUpstream(subslice, arrayOf(primary, *transactions))
    }

    private fun assertPrimaryRehash(
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

    private fun assertEqualSubsliceWithPrimary(
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
            assertThat(merkleTree.verifyTransaction(it.hash)).isTrue()
        }
    }

    private fun assertVerificationIndividually(
        merkleTree: MerkleTree, primary: Hashing,
        transactions: Array<out Hashing>
    ) {
        assertVerificationIndividually(
            merkleTree = merkleTree,
            transactions = transactions.fastPrefixAdd(primary)
        )
    }


    private fun logMerkle(
        ts: Array<out Hashing>,
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
        primary: Hashing,
        ts: Array<out Hashing>,
        tree: MerkleTree
    ) {
        logMerkle(ts, tree)
        Logger.debug {
            "Coinbase is ${primary.hash.base64Encoded()}"
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        private val primary7 = EmptyHashing()
        private val tree7WithPrimary = factory.create(hashers, primary7, ts7)

        private val begin8 = random.randomInt(size - 8)
        private val ts8 = base.sliceArray(begin8 until begin8 + 8)
        private val tree8 = factory.create(hashers, ts8)

        private val leafRange8 = 7 until 15
        private val upstreamRange8 = 0 until 7

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts8, tree8)

            //Root is present
            assertThat(tree8.hash).isNotNull()
            val nakedTree = tree8.collapsedTree.toTypedArray()

            //Assert all transactions match as leaves in the tree.
            assertEqualSubslice(
                nakedTree.sliceArray(leafRange8), ts8
            )
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
        fun `merkle tree primary recreation`() {
            //Log constructed merkle
            logMerkle(primary7, ts7, tree7WithPrimary)

            val treeClone = tree7WithPrimary.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(upstreamRange8), primary7, ts7)

            val primary7Replace = EmptyHashing()

            //Recalculate only primary propagation
            tree7WithPrimary.buildFromPrimary(primary7Replace)

            //Log constructed merkle
            logMerkle(primary7Replace, ts7, tree7WithPrimary)

            //Root is present
            assertThat(tree7WithPrimary.hash).isNotNull()
            val nakedTree = tree7WithPrimary.collapsedTree.toTypedArray()

            //Assert all transactions + primary match as leaves in the tree.
            assertEqualSubsliceWithPrimary(
                nakedTree.sliceArray(leafRange8), primary7Replace, ts7
            )

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(
                nakedTree.sliceArray(upstreamRange8), primary7Replace, ts7
            )

            //Verify primary update triggered left branch rehash up to root.
            assertPrimaryRehash(
                arrayOf(0, 1, 3, 7), treeClone, tree7WithPrimary
            )
        }

        @Test
        fun `all transaction verification`() {
            //Log constructed merkle
            logMerkle(primary7, ts7, tree7WithPrimary)
            assertThat(tree7WithPrimary.hash).isNotNull()
            assertThat(
                tree7WithPrimary.verifyBlockTransactions(primary7, ts7)
            ).isTrue()
            Logger.debug {
                "Balanced 7-transaction tree with payout is correct"
            }
        }


    }

    @Nested
    inner class UnbalancedMerkleTree {
        private val begin5 = random.randomInt(size - 5)
        private val ts5 = base.sliceArray(begin5 until begin5 + 5)
        private val primary5 = EmptyHashing()
        private val tree5WithPrimary = factory.create(hashers, primary5, ts5)

        private val begin6 = random.randomInt(size - 6)
        private val ts6 = base.sliceArray(begin6 until begin6 + 6)
        private val tree6 = factory.create(hashers, ts6)

        private val leafRange6 = 6 until 12
        private val upstreamRange6 = 0 until 6

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts6, tree6)

            //Root is present
            assertThat(tree6.hash).isNotNull()
            val nakedTree = tree6.collapsedTree.toTypedArray()

            //Assert all transactions + primary match as leaves in the tree.
            assertEqualSubslice(nakedTree.sliceArray(leafRange6), ts6)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(upstreamRange6), ts6)
        }

        @Test
        fun `merkle tree primary recreation`() {
            //Log constructed merkle
            logMerkle(primary5, ts5, tree5WithPrimary)

            val treeClone = tree5WithPrimary.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(0..5), primary5, ts5)

            val primary5Replace = EmptyHashing()

            //Recalculate only coinbase propagation
            tree5WithPrimary.buildFromPrimary(primary5Replace)

            //Log constructed merkle
            logMerkle(primary5Replace, ts5, tree5WithPrimary)

            //Root is present
            assertThat(tree5WithPrimary.hash).isNotNull()
            val nakedTree = tree5WithPrimary.collapsedTree.toTypedArray()

            //Assert all transactions + primary match as leaves in the tree.
            assertEqualSubsliceWithPrimary(nakedTree.sliceArray(6..11), primary5Replace, ts5)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..5), primary5Replace, ts5)

            //Verify primary update triggered left branch rehash up to root.
            assertPrimaryRehash(
                arrayOf(0, 1, 3, 6), treeClone,
                tree5WithPrimary
            )
        }

        @Test
        fun `merkle tree creation with primary`() {

            //Log constructed merkle
            logMerkle(primary5, ts5, tree5WithPrimary)


            //Root is present
            assertThat(tree5WithPrimary.hash).isNotNull()
            val nakedTree = tree5WithPrimary.collapsedTree.toTypedArray()

            //Assert all transactions + coinbase match as leaves in the tree.
            assertEqualSubsliceWithPrimary(nakedTree.sliceArray(6..11), primary5, ts5)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(0..5), primary5, ts5)
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts5, tree5WithPrimary)
            assertVerificationIndividually(tree5WithPrimary, primary5, ts5)
            Logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }

            val tree7 = factory.create(hashers, ts7)

            //Log constructed merkle
            logMerkle(ts7, tree7)
            assertVerificationIndividually(tree7, ts7)
            Logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {

            val primary6 = EmptyHashing()

            val tree6WithPrimary = factory.create(hashers, primary6, ts6)
            //Log constructed merkle
            logMerkle(primary6, ts6, tree6WithPrimary)

            assertThat(tree6WithPrimary.hash).isNotNull()
            assertThat(
                tree6WithPrimary.verifyBlockTransactions(primary6, ts6)
            ).isTrue()

            Logger.debug {
                "Unbalanced 6-transaction tree with payout is correct"
            }


            //Log constructed merkle
            logMerkle(primary5, ts5, tree5WithPrimary)

            assertThat(tree5WithPrimary.hash).isNotNull()
            assertThat(
                tree5WithPrimary.verifyBlockTransactions(primary5, ts5)
            ).isTrue()

            Logger.debug {
                "Unbalanced 5-transaction tree with payout is correct"
            }

        }

    }

    @Test
    fun `merkle tree creation with just root`() {
        val begin = random.randomInt(size - 1)

        val ts = arrayOf(base[begin])
        val tree = factory.create(hashers, ts)

        //Log constructed merkle
        logMerkle(ts, tree)

        assertThat(tree.hash).isNotNull()
        //Root matches the only transaction.
        assertThat(tree.collapsedTree[1].bytes).containsExactly(*ts[0].hash.bytes)
        assertThat(tree.hash.bytes).containsExactly(*hashers.applyHash(ts[0].hash + ts[0].hash).bytes)
        assertThat(tree.collapsedTree.size).isEqualTo(2)
    }
}
