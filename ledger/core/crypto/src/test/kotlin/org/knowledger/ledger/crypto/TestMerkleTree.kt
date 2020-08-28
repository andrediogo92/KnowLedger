package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.crypto.storage.MerkleTreeFactoryImpl
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.tinylog.kotlin.Logger

/**
 *
 */
class TestMerkleTree {
    private val hashers: Hashers = defaultHasher
    private val factory: MerkleTreeFactory = MerkleTreeFactoryImpl()

    class EmptyHashing : Hashing {
        override val hash: Hash = random.randomHash()
    }

    private val size = 24
    private val base = Array<Hashing>(size) { EmptyHashing() }
    private val begin7 = random.randomInt(size - 7)
    private val ts7 = base.sliceArray(begin7 until begin7 + 7)


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
            assertEqualSubslice(nakedTree.sliceArray(leafRange8), ts8)
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts8, tree8)
            assertVerificationIndividually(tree8, ts8)
            Logger.debug { "Balanced 8-transaction tree is correct" }
        }

        @Test
        fun `merkle tree primary recreation`() {
            //Log constructed merkle
            logMerkle(primary7, ts7, tree7WithPrimary)

            val treeClone = tree7WithPrimary.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(upstreamRange8), primary7, ts7, hashers)

            val primary7Replace = EmptyHashing()

            //Recalculate only primary propagation
            tree7WithPrimary.buildFromPrimary(primary7Replace)

            //Log constructed merkle
            logMerkle(primary7Replace, ts7, tree7WithPrimary)

            //Root is present
            assertThat(tree7WithPrimary.hash).isNotNull()
            val nakedTree = tree7WithPrimary.collapsedTree.toTypedArray()

            //Assert all transactions + primary match as leaves in the tree.
            assertEqualSubsliceWithPrimary(nakedTree.sliceArray(leafRange8), primary7Replace, ts7)

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(nakedTree.sliceArray(upstreamRange8), primary7Replace, ts7, hashers)

            //Verify primary update triggered left branch rehash up to root.
            assertPrimaryRehash(arrayOf(0, 1, 3, 7), treeClone, tree7WithPrimary)
        }

        @Test
        fun `all transaction verification`() {
            //Log constructed merkle
            logMerkle(primary7, ts7, tree7WithPrimary)
            assertThat(tree7WithPrimary.hash).isNotNull()
            assertThat(tree7WithPrimary.verifyBlockTransactions(primary7, ts7)).isTrue()
            Logger.debug { "Balanced 7-transaction tree with payout is correct" }
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
            assertUpstream(nakedTree.sliceArray(upstreamRange6), ts6, hashers)
        }

        @Test
        fun `merkle tree primary recreation`() {
            //Log constructed merkle
            logMerkle(primary5, ts5, tree5WithPrimary)

            val treeClone = tree5WithPrimary.collapsedTree.toTypedArray()

            //Assert all levels of the tree have correct hashing upstream.
            assertUpstream(treeClone.sliceArray(0..5), primary5, ts5, hashers)

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
            assertUpstream(nakedTree.sliceArray(0..5), primary5Replace, ts5, hashers)

            //Verify primary update triggered left branch rehash up to root.
            assertPrimaryRehash(arrayOf(0, 1, 3, 6), treeClone, tree5WithPrimary)
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
            assertUpstream(nakedTree.sliceArray(0..5), primary5, ts5, hashers)
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts5, tree5WithPrimary)
            assertVerificationIndividually(tree5WithPrimary, primary5, ts5)
            Logger.debug { "Unbalanced 5-transaction tree is correct" }

            val tree7 = factory.create(hashers, ts7)

            //Log constructed merkle
            logMerkle(ts7, tree7)
            assertVerificationIndividually(tree7, ts7)
            Logger.debug { "Unbalanced 7-transaction tree is correct" }
        }

        @Test
        fun `all transaction verification`() {

            val primary6 = EmptyHashing()

            val tree6WithPrimary = factory.create(hashers, primary6, ts6)
            //Log constructed merkle
            logMerkle(primary6, ts6, tree6WithPrimary)

            assertThat(tree6WithPrimary.hash).isNotNull()
            assertThat(tree6WithPrimary.verifyBlockTransactions(primary6, ts6)).isTrue()

            Logger.debug { "Unbalanced 6-transaction tree with payout is correct" }


            //Log constructed merkle
            logMerkle(primary5, ts5, tree5WithPrimary)

            assertThat(tree5WithPrimary.hash).isNotNull()
            assertThat(tree5WithPrimary.verifyBlockTransactions(primary5, ts5)).isTrue()

            Logger.debug { "Unbalanced 5-transaction tree with payout is correct" }

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

        val hash = hashers.applyHash(ts[0].hash + ts[0].hash)
        assertThat(tree.hash.bytes).containsExactly(*hash.bytes)
        assertThat(tree.collapsedTree.size).isEqualTo(2)
    }
}
