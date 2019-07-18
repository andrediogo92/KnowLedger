package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.test.applyHashInPairs
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree
import org.tinylog.kotlin.Logger

class TestMerkleTree {
    private val hasher =
        LedgerConfiguration.DEFAULT_CRYPTER

    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    private val ts7 = generateXTransactions(id, 7)


    private fun logMerkle(
        ts: List<Transaction>,
        tree: MerkleTree
    ) {
        tree.nakedTree.forEachIndexed { i, it ->
            Logger.debug {
                "Naked tree @$i -> ${it.print}"
            }
        }

        tree.levelIndexes.forEachIndexed { i, it ->
            Logger.debug {
                "Level @$i -> Starts from $it"
            }
        }

        ts.forEachIndexed { i, it ->
            Logger.debug {
                "Transactions @$i -> ${it.hashId.print}"
            }
        }
    }

    private fun logMerkle(
        coinbase: Coinbase,
        ts: List<Transaction>,
        tree: MerkleTree
    ) {
        logMerkle(ts, tree)
        Logger.debug {
            "Coinbase is ${coinbase.hashId.print}"
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        private val ts8 = generateXTransactions(id, 8, hasher)
        private val tree8 = StorageUnawareMerkleTree(hasher, ts8.toTypedArray())

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts8, tree8)

            //Root is present
            assertThat(tree8.root).isNotNull()
            val nakedTree = tree8.nakedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[7].bytes).containsExactly(*ts8[0].hashId.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts8[1].hashId.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts8[2].hashId.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts8[3].hashId.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts8[4].hashId.bytes)
            assertThat(nakedTree[12].bytes).containsExactly(*ts8[5].hashId.bytes)
            assertThat(nakedTree[13].bytes).containsExactly(*ts8[6].hashId.bytes)
            assertThat(nakedTree[14].bytes).containsExactly(*ts8[7].hashId.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *hasher.applyHash(ts8[0].hashId + ts8[1].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *hasher.applyHash(ts8[2].hashId + ts8[3].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *hasher.applyHash(ts8[4].hashId + ts8[5].hashId).bytes
            )
            assertThat(nakedTree[6].bytes).containsExactly(
                *hasher.applyHash(ts8[6].hashId + ts8[7].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 + hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts8[0].hashId,
                        ts8[1].hashId,
                        ts8[2].hashId,
                        ts8[3].hashId
                    )
                ).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 + hashId of transactions 3 + 4
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts8[4].hashId,
                        ts8[5].hashId,
                        ts8[6].hashId,
                        ts8[7].hashId
                    )
                ).bytes

            )
            assertThat(tree8.root.bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts8[0].hashId,
                        ts8[1].hashId,
                        ts8[2].hashId,
                        ts8[3].hashId,
                        ts8[4].hashId,
                        ts8[5].hashId,
                        ts8[6].hashId,
                        ts8[7].hashId
                    )
                ).bytes

            )
        }

        @Test
        fun `single verification of transactions`() {


            //Log constructed merkle
            logMerkle(ts8, tree8)

            assertThat(tree8.root).isNotNull()
            assertThat(
                tree8.verifyTransaction(ts8[0].hashId)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[1].hashId)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[2].hashId)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[3].hashId)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[4].hashId)
            ).isTrue()
            assertThat(
                tree8.verifyTransaction(ts8[5].hashId)
            ).isTrue()

            Logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {
            val coinbase7 = generateCoinbase(id, ts7)
            val tree7WithCoinbase = StorageUnawareMerkleTree(hasher, coinbase7, ts7.toTypedArray())


            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)

            assertThat(tree7WithCoinbase.root).isNotNull()
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
        private val tree5WithCoinbase = StorageUnawareMerkleTree(hasher, coinbase5, ts5.toTypedArray())
        private val ts6 = generateXTransactions(id, 6)
        private val tree6 = StorageUnawareMerkleTree(hasher, ts6.toTypedArray())


        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts6, tree6)

            //Root is present
            assertThat(tree6.root).isNotNull()
            val nakedTree = tree6.nakedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[6].bytes).containsExactly(*ts6[0].hashId.bytes)
            assertThat(nakedTree[7].bytes).containsExactly(*ts6[1].hashId.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts6[2].hashId.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts6[3].hashId.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts6[4].hashId.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts6[5].hashId.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *hasher.applyHash(ts6[0].hashId + ts6[1].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *hasher.applyHash(ts6[2].hashId + ts6[3].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *hasher.applyHash(ts6[4].hashId + ts6[5].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts6[0].hashId,
                        ts6[1].hashId,
                        ts6[2].hashId,
                        ts6[3].hashId
                    )
                ).bytes
            )
            //One level to the right is a hashId of the hashId
            //of transactions 1 + 2 * 2
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts6[4].hashId,
                        ts6[5].hashId,
                        ts6[4].hashId,
                        ts6[5].hashId
                    )
                ).bytes
            )
            //Root is everything else.
            assertThat(tree6.root.bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts6[0].hashId,
                        ts6[1].hashId,
                        ts6[2].hashId,
                        ts6[3].hashId,
                        ts6[4].hashId,
                        ts6[5].hashId
                    )
                ).bytes
            )
        }

        @Test
        fun `merkle tree creation with coinbase`() {

            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)


            //Root is present
            assertThat(tree5WithCoinbase.root).isNotNull()
            val nakedTree = tree5WithCoinbase.nakedTree
            //Three levels to the left is first transaction.
            //Said transaction is the coinbase
            assertThat(nakedTree[6].bytes).containsExactly(*coinbase5.hashId.bytes)
            assertThat(nakedTree[7].bytes).containsExactly(*ts5[0].hashId.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts5[1].hashId.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts5[2].hashId.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts5[3].hashId.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts5[4].hashId.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *hasher.applyHash(coinbase5.hashId + ts5[0].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *hasher.applyHash(ts5[1].hashId + ts5[2].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *hasher.applyHash(ts5[3].hashId + ts5[4].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and the hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        coinbase5.hashId,
                        ts5[0].hashId,
                        ts5[1].hashId,
                        ts5[2].hashId
                    )
                ).bytes
            )
            //One level to the right is a hashId of the hashId of
            //transactions 5 + 6 * 2
            assertThat(nakedTree[2].bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        ts5[3].hashId,
                        ts5[4].hashId,
                        ts5[3].hashId,
                        ts5[4].hashId
                    )
                ).bytes
            )
            //Root is everything else.
            assertThat(tree5WithCoinbase.root.bytes).containsExactly(
                *applyHashInPairs(
                    hasher,
                    arrayOf(
                        coinbase5.hashId,
                        ts5[0].hashId,
                        ts5[1].hashId,
                        ts5[2].hashId,
                        ts5[3].hashId,
                        ts5[4].hashId
                    )
                ).bytes
            )
        }

        @Test
        fun `single verification of transactions`() {
            //Log constructed merkle
            logMerkle(ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.root).isNotNull()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[0].hashId)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[1].hashId)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[2].hashId)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[3].hashId)
            ).isTrue()
            assertThat(
                tree5WithCoinbase.verifyTransaction(ts5[4].hashId)
            ).isTrue()

            Logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }

            val tree7 = StorageUnawareMerkleTree(hasher, ts7.toTypedArray())

            //Log constructed merkle
            logMerkle(ts7, tree7)


            assertThat(tree7.root).isNotNull()
            assertThat(
                tree7.verifyTransaction(ts7[0].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[1].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[2].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[3].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[4].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[5].hashId)
            ).isTrue()
            assertThat(
                tree7.verifyTransaction(ts7[6].hashId)
            ).isTrue()

            Logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {

            val coinbase6 = generateCoinbase(id, ts6)
            val tree6WithCoinbase = StorageUnawareMerkleTree(hasher, coinbase6, ts6.toTypedArray())

            //Log constructed merkle
            logMerkle(coinbase6, ts6, tree6WithCoinbase)

            assertThat(tree6WithCoinbase.root).isNotNull()
            assertThat(
                tree6WithCoinbase.verifyBlockTransactions(coinbase6, ts6.toTypedArray())
            ).isTrue()

            Logger.debug {
                "Unbalanced 6-transaction tree with payout is correct"
            }


            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.root).isNotNull()
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
        val ts = generateXTransactions(id, 1, hasher)
        val tree = StorageUnawareMerkleTree(hasher, ts.toTypedArray())

        //Log constructed merkle
        logMerkle(ts, tree)

        assertThat(tree.root).isNotNull()
        //Root matches the only transaction.
        assertThat(tree.root.bytes).containsExactly(*ts[0].hashId.bytes)
        assertThat(tree.nakedTree.size).isEqualTo(1)
    }
}
