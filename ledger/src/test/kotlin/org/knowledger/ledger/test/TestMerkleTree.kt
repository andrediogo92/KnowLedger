package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.ledger.core.test.applyHashInPairs
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.tinylog.kotlin.Logger

/**
 * TODO: Move low-level testing to crypto module.
 */
class TestMerkleTree {
    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    private val ts7 = generateXTransactions(id, 7)


    private fun logMerkle(
        ts: List<HashedTransaction>,
        tree: MerkleTree
    ) {
        tree.collapsedTree.forEachIndexed { i, it ->
            Logger.debug {
                "Naked tree @$i -> ${it.print}"
            }
        }

        tree.levelIndex.forEachIndexed { i, it ->
            Logger.debug {
                "Level @$i -> Starts from $it"
            }
        }

        ts.forEachIndexed { i, it ->
            Logger.debug {
                "Transactions @$i -> ${it.hash.print}"
            }
        }
    }

    private fun logMerkle(
        coinbase: HashedCoinbase,
        ts: List<HashedTransaction>,
        tree: MerkleTree
    ) {
        logMerkle(ts, tree)
        Logger.debug {
            "Coinbase is ${coinbase.hash.print}"
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        private val ts8 = generateXTransactions(id, 8, testHasher)
        private val tree8 = MerkleTreeImpl(testHasher, ts8.toTypedArray())

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts8, tree8)

            //Root is present
            assertThat(tree8.hash).isNotNull()
            val nakedTree = tree8.collapsedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[7].bytes).containsExactly(*ts8[0].hash.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts8[1].hash.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts8[2].hash.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts8[3].hash.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts8[4].hash.bytes)
            assertThat(nakedTree[12].bytes).containsExactly(*ts8[5].hash.bytes)
            assertThat(nakedTree[13].bytes).containsExactly(*ts8[6].hash.bytes)
            assertThat(nakedTree[14].bytes).containsExactly(*ts8[7].hash.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *testHasher.applyHash(ts8[0].hash + ts8[1].hash).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *testHasher.applyHash(ts8[2].hash + ts8[3].hash).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *testHasher.applyHash(ts8[4].hash + ts8[5].hash).bytes
            )
            assertThat(nakedTree[6].bytes).containsExactly(
                *testHasher.applyHash(ts8[6].hash + ts8[7].hash).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 + hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts8[0].hash,
                        ts8[1].hash,
                        ts8[2].hash,
                        ts8[3].hash
                    )
                ).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 + hashId of transactions 3 + 4
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts8[4].hash,
                        ts8[5].hash,
                        ts8[6].hash,
                        ts8[7].hash
                    )
                ).bytes

            )
            assertThat(tree8.hash.bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts8[0].hash,
                        ts8[1].hash,
                        ts8[2].hash,
                        ts8[3].hash,
                        ts8[4].hash,
                        ts8[5].hash,
                        ts8[6].hash,
                        ts8[7].hash
                    )
                ).bytes

            )
        }

        @Test
        fun `single verification of transactions`() {


            //Log constructed merkle
            logMerkle(ts8, tree8)

            assertThat(tree8.hash).isNotNull()
            assertThat(
                tree8.verifyTransaction(ts8[0].hash)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[1].hash)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[2].hash)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[3].hash)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[4].hash)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[5].hash)
            ).isTrue()

            Logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {
            val coinbase7 = generateCoinbase(id, ts7)
            val tree7WithCoinbase =
                MerkleTreeImpl(testHasher, coinbase7, ts7.toTypedArray())


            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)

            assertThat(tree7WithCoinbase.hash).isNotNull()
            assertThat(
                tree7WithCoinbase.verifyBlockTransactions(coinbase7, ts7.toTypedArray())
            ).isTrue()

            Logger.debug {
                "Balanced 7-transaction tree with payout is correct"
            }
        }


    }

    @Nested
    inner class UnbalancedMerkleTree {
        private val ts5 = generateXTransactions(id, 5)
        private val coinbase5 = generateCoinbase(id, ts5)
        private val tree5WithCoinbase =
            MerkleTreeImpl(testHasher, coinbase5, ts5.toTypedArray())
        private val ts6 = generateXTransactions(id, 6)
        private val tree6 = MerkleTreeImpl(testHasher, ts6.toTypedArray())


        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts6, tree6)

            //Root is present
            assertThat(tree6.hash).isNotNull()
            val nakedTree = tree6.collapsedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[6].bytes).containsExactly(*ts6[0].hash.bytes)
            assertThat(nakedTree[7].bytes).containsExactly(*ts6[1].hash.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts6[2].hash.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts6[3].hash.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts6[4].hash.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts6[5].hash.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *testHasher.applyHash(ts6[0].hash + ts6[1].hash).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *testHasher.applyHash(ts6[2].hash + ts6[3].hash).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *testHasher.applyHash(ts6[4].hash + ts6[5].hash).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts6[0].hash,
                        ts6[1].hash,
                        ts6[2].hash,
                        ts6[3].hash
                    )
                ).bytes
            )
            //One level to the right is a hashId of the hashId
            //of transactions 1 + 2 * 2
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts6[4].hash,
                        ts6[5].hash,
                        ts6[4].hash,
                        ts6[5].hash
                    )
                ).bytes
            )
            //Root is everything else.
            assertThat(tree6.hash.bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts6[0].hash,
                        ts6[1].hash,
                        ts6[2].hash,
                        ts6[3].hash,
                        ts6[4].hash,
                        ts6[5].hash
                    )
                ).bytes
            )
        }

        @Test
        fun `merkle tree creation with coinbase`() {

            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)


            //Root is present
            assertThat(tree5WithCoinbase.hash).isNotNull()
            val nakedTree = tree5WithCoinbase.collapsedTree
            //Three levels to the left is first transaction.
            //Said transaction is the coinbase
            assertThat(nakedTree[6].bytes).containsExactly(*coinbase5.hash.bytes)
            assertThat(nakedTree[7].bytes).containsExactly(*ts5[0].hash.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts5[1].hash.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts5[2].hash.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts5[3].hash.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts5[4].hash.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *testHasher.applyHash(coinbase5.hash + ts5[0].hash).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *testHasher.applyHash(ts5[1].hash + ts5[2].hash).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *testHasher.applyHash(ts5[3].hash + ts5[4].hash).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and the hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        coinbase5.hash,
                        ts5[0].hash,
                        ts5[1].hash,
                        ts5[2].hash
                    )
                ).bytes
            )
            //One level to the right is a hashId of the hashId of
            //transactions 5 + 6 * 2
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        ts5[3].hash,
                        ts5[4].hash,
                        ts5[3].hash,
                        ts5[4].hash
                    )
                ).bytes
            )
            //Root is everything else.
            assertThat(tree5WithCoinbase.hash.bytes).containsExactly(
                *applyHashInPairs(
                    testHasher,
                    arrayOf(
                        coinbase5.hash,
                        ts5[0].hash,
                        ts5[1].hash,
                        ts5[2].hash,
                        ts5[3].hash,
                        ts5[4].hash
                    )
                ).bytes
            )
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.hash).isNotNull()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[0].hash)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[1].hash)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[2].hash)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[3].hash)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[4].hash)
            ).isTrue()

            Logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }

            val tree7 = MerkleTreeImpl(testHasher, ts7.toTypedArray())

            //Log constructed merkle
            logMerkle(ts7, tree7)


            assertThat(tree7.hash).isNotNull()
            assertThat(
                tree7.verifyTransaction(ts7[0].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[1].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[2].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[3].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[4].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[5].hash)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[6].hash)
            ).isTrue()

            Logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {

            val coinbase6 = generateCoinbase(id, ts6)
            val tree6WithCoinbase =
                MerkleTreeImpl(testHasher, coinbase6, ts6.toTypedArray())

            //Log constructed merkle
            logMerkle(coinbase6, ts6, tree6WithCoinbase)

            assertThat(tree6WithCoinbase.hash).isNotNull()
            assertThat(
                tree6WithCoinbase.verifyBlockTransactions(coinbase6, ts6.toTypedArray())
            ).isTrue()

            Logger.debug {
                "Unbalanced 6-transaction tree with payout is correct"
            }


            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.hash).isNotNull()
            assertThat(
                tree5WithCoinbase.verifyBlockTransactions(coinbase5, ts5.toTypedArray())
            ).isTrue()

            Logger.debug {
                "Unbalanced 5-transaction tree with payout is correct"
            }

        }

    }


    @Test
    fun `merkle tree creation with just root`() {
        val ts = generateXTransactions(id, 1, testHasher)
        val tree = MerkleTreeImpl(testHasher, ts.toTypedArray())

        //Log constructed merkle
        logMerkle(ts, tree)

        assertThat(tree.hash).isNotNull()
        //Root matches the only transaction.
        assertThat(tree.hash.bytes).containsExactly(*ts[0].hash.bytes)
        assertThat(tree.collapsedTree.size).isEqualTo(1)
    }
}
