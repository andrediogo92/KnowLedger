package pt.um.masb.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TUnit
import pt.um.masb.ledger.data.TemperatureData
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.test.utils.applyHashInPairs
import pt.um.masb.ledger.test.utils.crypter
import pt.um.masb.ledger.test.utils.generateCoinbase
import pt.um.masb.ledger.test.utils.makeXTransactions
import pt.um.masb.ledger.test.utils.randomDouble
import java.math.BigDecimal

class TestMerkleTree {

    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    private val ts7 = makeXTransactions(id, 7)


    private fun logMerkle(
        ts: List<Transaction>,
        tree: MerkleTree
    ) {
        tree.collapsedTree.forEachIndexed { i, it ->
            logger.debug {
                "Naked tree @$i -> ${it.print}"
            }
        }

        tree.levelIndex.forEachIndexed { i, it ->
            logger.debug {
                "Level @$i -> Starts from $it"
            }
        }

        ts.forEachIndexed { i, it ->
            logger.debug {
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
        logger.debug {
            "Coinbase is ${coinbase.hashId.print}"
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        private val ts8 = makeXTransactions(id, 8)
        private val tree8 = MerkleTree.buildMerkleTree(ts8)

        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts8, tree8)

            //Root is present
            assertThat(tree8.root).isNotNull()
            val nakedTree = tree8.collapsedTree
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
                *crypter.applyHash(ts8[0].hashId + ts8[1].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *crypter.applyHash(ts8[2].hashId + ts8[3].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *crypter.applyHash(ts8[4].hashId + ts8[5].hashId).bytes
            )
            assertThat(nakedTree[6].bytes).containsExactly(
                *crypter.applyHash(ts8[6].hashId + ts8[7].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 + hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    crypter,
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
                    crypter,
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
                    crypter,
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

            logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {
            val coinbase7 = generateCoinbase(id, ts7)
            val tree7WithCoinbase = MerkleTree.buildMerkleTree(coinbase7, ts7)


            //Log constructed merkle
            logMerkle(coinbase7, ts7, tree7WithCoinbase)

            assertThat(tree7WithCoinbase.root).isNotNull()
            assertThat(
                tree7WithCoinbase.verifyBlockTransactions(coinbase7, ts7)
            ).isTrue()

            logger.debug {
                "Balanced 7-transaction tree with coinbase is correct"
            }
        }


    }

    @Nested
    inner class UnbalancedMerkleTree {
        private val ts5 = makeXTransactions(id, 5)
        private val coinbase5 = generateCoinbase(id, ts5)
        private val tree5WithCoinbase = MerkleTree.buildMerkleTree(coinbase5, ts5)
        private val ts6 = makeXTransactions(id, 6)
        private val tree6 = MerkleTree.buildMerkleTree(ts6)


        @Test
        fun `merkle tree creation`() {

            //Log constructed merkle
            logMerkle(ts6, tree6)

            //Root is present
            assertThat(tree6.root).isNotNull()
            val nakedTree = tree6.collapsedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[6].bytes).containsExactly(*ts6[0].hashId.bytes)
            assertThat(nakedTree[7].bytes).containsExactly(*ts6[1].hashId.bytes)
            assertThat(nakedTree[8].bytes).containsExactly(*ts6[2].hashId.bytes)
            assertThat(nakedTree[9].bytes).containsExactly(*ts6[3].hashId.bytes)
            assertThat(nakedTree[10].bytes).containsExactly(*ts6[4].hashId.bytes)
            assertThat(nakedTree[11].bytes).containsExactly(*ts6[5].hashId.bytes)
            //Two levels in to the left is a hashId of transaction 1 + 2.
            assertThat(nakedTree[3].bytes).containsExactly(
                *crypter.applyHash(ts6[0].hashId + ts6[1].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *crypter.applyHash(ts6[2].hashId + ts6[3].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *crypter.applyHash(ts6[4].hashId + ts6[5].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    crypter,
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
                    crypter,
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
                    crypter,
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
            val nakedTree = tree5WithCoinbase.collapsedTree
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
                *crypter.applyHash(coinbase5.hashId + ts5[0].hashId).bytes
            )
            assertThat(nakedTree[4].bytes).containsExactly(
                *crypter.applyHash(ts5[1].hashId + ts5[2].hashId).bytes
            )
            assertThat(nakedTree[5].bytes).containsExactly(
                *crypter.applyHash(ts5[3].hashId + ts5[4].hashId).bytes
            )
            //One level to the left is a hashId of the hashId of
            //transactions 1 + 2 and the hashId of transactions 3 + 4
            assertThat(nakedTree[1].bytes).containsExactly(
                *applyHashInPairs(
                    crypter,
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
                    crypter,
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
                    crypter,
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

            logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }

            val tree7 = MerkleTree.buildMerkleTree(ts7)

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

            logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `all transaction verification`() {

            val coinbase6 = generateCoinbase(id, ts6)
            val tree6WithCoinbase = MerkleTree.buildMerkleTree(coinbase6, ts6)

            //Log constructed merkle
            logMerkle(coinbase6, ts6, tree6WithCoinbase)

            assertThat(tree6WithCoinbase.root).isNotNull()
            assertThat(
                tree6WithCoinbase.verifyBlockTransactions(coinbase6, ts6)
            ).isTrue()

            logger.debug {
                "Unbalanced 6-transaction tree with coinbase is correct"
            }


            //Log constructed merkle
            logMerkle(coinbase5, ts5, tree5WithCoinbase)

            assertThat(tree5WithCoinbase.root).isNotNull()
            assertThat(
                tree5WithCoinbase.verifyBlockTransactions(coinbase5, ts5)
            ).isTrue()

            logger.debug {
                "Unbalanced 5-transaction tree with coinbase is correct"
            }

        }

    }


    @Test
    fun `merkle tree creation with just root`() {
        val ts = listOf(
            Transaction(
                id[0].privateKey,
                id[0].publicKey,
                PhysicalData(
                    TemperatureData(
                        temperature = BigDecimal(randomDouble() * 100),
                        unit = TUnit.CELSIUS
                    )
                )
            )
        )
        val tree = MerkleTree.buildMerkleTree(ts)

        //Log constructed merkle
        logMerkle(ts, tree)

        assertThat(tree.root).isNotNull()
        //Root matches the only transaction.
        assertThat(tree.root.bytes).containsExactly(*ts[0].hashId.bytes)
        assertThat(tree.collapsedTree.size).isEqualTo(1)
    }

    companion object : KLogging()


}
