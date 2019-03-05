package pt.um.lei.masb.test

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.test.utils.crypter
import pt.um.lei.masb.test.utils.generateCoinbase
import pt.um.lei.masb.test.utils.makeXTransactions
import java.math.BigDecimal
import java.security.Security
import java.util.*

class TestMerkleTree {

    private val id = arrayOf(
        Ident("test1"),
        Ident("test2")
    )

    private lateinit var tree: MerkleTree

    private lateinit var ts: List<Transaction>

    private lateinit var coinbase: Coinbase

    init {
        logProviders()
    }

    private fun logMerkle(
        ts: List<Transaction>,
        tree: MerkleTree
    ) {
        tree.collapsedTree.forEachIndexed { i, it ->
            logger.debug {
                "Naked tree @$i -> ${it.print()}"
            }
        }

        tree.levelIndex.forEachIndexed { i, it ->
            logger.debug {
                "Level @$i -> Starts from $it"
            }
        }

        ts.forEachIndexed { i, it ->
            logger.debug {
                "Transactions @$i -> ${it.hashId.print()}"
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
            "Coinbase is ${coinbase.hashId.print()}"
        }
    }


    private fun logProviders() {
        logger.info {
            Security.getProviders().joinToString(" | ") {
                it.name
            }
        }
    }


    @Nested
    inner class BalancedMerkleTree {
        @Test
        fun `Test merkle tree creation`() {
            ts = makeXTransactions(id, 8)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)

            //Root is present
            assertThat(tree.root).isNotNull()
            val nakedTree = tree.collapsedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[7]).containsAll(*ts[0].hashId)
            assertThat(nakedTree[8]).containsAll(*ts[1].hashId)
            assertThat(nakedTree[9]).containsAll(*ts[2].hashId)
            assertThat(nakedTree[10]).containsAll(* ts[3].hashId)
            assertThat(nakedTree[11]).containsAll(* ts[4].hashId)
            assertThat(nakedTree[12]).containsAll(* ts[5].hashId)
            assertThat(nakedTree[13]).containsAll(* ts[6].hashId)
            assertThat(nakedTree[14]).containsAll(* ts[7].hashId)
            //Two levels in to the left is a hash of transaction 1 + 2.
            assertThat(
                nakedTree[3]
            ).containsAll(
                *
                crypter.applyHash(ts[0].hashId + ts[1].hashId)
            )
            assertThat(
                nakedTree[4]
            ).containsAll(
                *
                crypter.applyHash(ts[2].hashId + ts[3].hashId)
            )
            assertThat(
                nakedTree[5]
            ).containsAll(
                *
                crypter.applyHash(ts[4].hashId + ts[5].hashId)
            )
            assertThat(
                nakedTree[6]
            ).containsAll(
                *
                crypter.applyHash(ts[6].hashId + ts[7].hashId)
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertThat(
                nakedTree[1]
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                            crypter.applyHash(ts[2].hashId + ts[3].hashId)
                )
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertThat(
                nakedTree[2]
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                            crypter.applyHash(ts[6].hashId + ts[7].hashId)
                )
            )
            assertThat(
                tree.root
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(
                        crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                                crypter.applyHash(ts[2].hashId + ts[3].hashId)
                    ) +
                            crypter.applyHash(
                                crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                                        crypter.applyHash(ts[6].hashId + ts[7].hashId)
                            )
                )
            )
        }

        @Test
        fun `Test single verification of transactions`() {


            ts = makeXTransactions(id, 8)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)

            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyTransaction(ts[0].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[1].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[2].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[3].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[4].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[5].hashId)
            ).isTrue()

            logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }

        @Test
        fun `Test all transaction verification`() {

            ts = makeXTransactions(id, 7)
            coinbase = generateCoinbase(id, ts)
            tree = MerkleTree.buildMerkleTree(coinbase, ts)

            //Log constructed merkle
            logMerkle(coinbase, ts, tree)

            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyBlockTransactions(coinbase, ts)
            ).isTrue()

            logger.debug {
                "Balanced 8-transaction tree is correct"
            }
        }


    }

    @Nested
    inner class UnbalancedMerkleTree {
        @Test
        fun `Test merkle tree creation`() {
            ts = makeXTransactions(id, 6)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)

            //Root is present
            assertThat(tree.root).isNotNull()
            val nakedTree = tree.collapsedTree
            //Three levels to the left is first transaction.
            assertThat(nakedTree[6]).containsAll(* ts[0].hashId)
            assertThat(nakedTree[7]).containsAll(* ts[1].hashId)
            assertThat(nakedTree[8]).containsAll(* ts[2].hashId)
            assertThat(nakedTree[9]).containsAll(* ts[3].hashId)
            assertThat(nakedTree[10]).containsAll(* ts[4].hashId)
            assertThat(nakedTree[11]).containsAll(* ts[5].hashId)
            //Two levels in to the left is a hash of transaction 1 + 2.
            assertThat(
                nakedTree[3]
            ).containsAll(
                *
                crypter.applyHash(ts[0].hashId + ts[1].hashId)
            )
            assertThat(
                nakedTree[4]
            ).containsAll(
                *
                crypter.applyHash(ts[2].hashId + ts[3].hashId)
            )
            assertThat(
                nakedTree[5]
            ).containsAll(
                *
                crypter.applyHash(ts[4].hashId + ts[5].hashId)
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertThat(
                nakedTree[1]
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                            crypter.applyHash(ts[2].hashId + ts[3].hashId)
                )
            )
            //One level to the right is a hash of the hash of transactions 1 + 2 * 2
            assertThat(
                nakedTree[2]
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                            crypter.applyHash(ts[4].hashId + ts[5].hashId)
                )
            )
            //Root is everything else.
            assertThat(
                tree.root
            ).containsAll(
                *
                crypter.applyHash(
                    crypter.applyHash(
                        crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                                crypter.applyHash(ts[2].hashId + ts[3].hashId)
                    ) +
                            crypter.applyHash(
                                crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                                        crypter.applyHash(ts[4].hashId + ts[5].hashId)
                            )
                )
            )

            @Test
            fun `Test merkle tree creation with coinbase`() {
                ts = makeXTransactions(id, 5)
                coinbase = generateCoinbase(id, ts)
                tree = MerkleTree.buildMerkleTree(coinbase, ts)

                //Log constructed merkle
                logMerkle(coinbase, ts, tree)


                //Root is present
                assertThat(tree.root).isNotNull()
                val nakedTree = tree.collapsedTree
                //Three levels to the left is first transaction.
                //Said transaction is the coinbase
                assertThat(nakedTree[6]).containsAll(* coinbase.hashId)
                assertThat(nakedTree[7]).containsAll(* ts[0].hashId)
                assertThat(nakedTree[8]).containsAll(* ts[1].hashId)
                assertThat(nakedTree[9]).containsAll(* ts[2].hashId)
                assertThat(nakedTree[10]).containsAll(* ts[3].hashId)
                assertThat(nakedTree[11]).containsAll(* ts[4].hashId)
                //Two levels in to the left is a hash of transaction 1 & 2.
                assertThat(
                    nakedTree[3]
                ).containsAll(
                    *
                    crypter.applyHash(coinbase.hashId + ts[0].hashId)
                )
                assertThat(
                    nakedTree[4]
                ).containsAll(
                    *
                    crypter.applyHash(ts[1].hashId + ts[2].hashId)
                )
                assertThat(
                    nakedTree[5]
                ).containsAll(
                    *
                    crypter.applyHash(ts[3].hashId + ts[4].hashId)
                )
                //One level to the left is a hash of the hash of transactions 1 & 2
                // + the hash of transactions 3 & 4
                assertThat(
                    nakedTree[1]
                ).containsAll(
                    *
                    crypter.applyHash(
                        crypter.applyHash(coinbase.hashId + ts[0].hashId) +
                                crypter.applyHash(ts[1].hashId + ts[2].hashId)
                    )
                )
                //One level to the right is a hash of the hash of transactions 5 + 6 * 2
                assertThat(
                    nakedTree[2]
                ).containsAll(
                    *
                    crypter.applyHash(
                        crypter.applyHash(ts[3].hashId + ts[4].hashId) +
                                crypter.applyHash(ts[3].hashId + ts[4].hashId)
                    )
                )
                //Root is everything else.
                assertThat(
                    tree.root
                ).containsAll(
                    *
                    crypter.applyHash(
                        crypter.applyHash(
                            crypter.applyHash(coinbase.hashId + ts[0].hashId) +
                                    crypter.applyHash(ts[1].hashId + ts[2].hashId)
                        ) +
                                crypter.applyHash(
                                    crypter.applyHash(ts[3].hashId + ts[4].hashId) +
                                            crypter.applyHash(ts[3].hashId + ts[4].hashId)
                                )
                    )
                )
            }
        }

        @Test
        fun `Test single verification of transactions`() {


            ts = makeXTransactions(id, 5)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)

            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyTransaction(ts[0].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[1].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[2].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[3].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[4].hashId)
            ).isTrue()

            logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }


            ts = makeXTransactions(id, 7)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)


            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyTransaction(ts[0].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[1].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[2].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[3].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[4].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[5].hashId)
            ).isTrue()
            assertThat(
                tree.verifyTransaction(ts[6].hashId)
            ).isTrue()

            logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }
        }

        @Test
        fun `Test all transaction verification`() {

            ts = makeXTransactions(id, 6)
            coinbase = generateCoinbase(id, ts)
            tree = MerkleTree.buildMerkleTree(coinbase, ts)

            //Log constructed merkle
            logMerkle(coinbase, ts, tree)

            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyBlockTransactions(coinbase, ts)
            ).isTrue()

            logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }

            ts = makeXTransactions(id, 5)
            coinbase = generateCoinbase(id, ts)
            tree = MerkleTree.buildMerkleTree(coinbase, ts)

            //Log constructed merkle
            logMerkle(coinbase, ts, tree)

            assertThat(tree.root).isNotNull()
            assertThat(
                tree.verifyBlockTransactions(coinbase, ts)
            ).isTrue()

            logger.debug {
                "Unbalanced 6-transaction tree is correct"
            }

        }

    }


    @Test
    fun `Test merkle tree creation with just root`() {
        val r = Random()
        val ts = listOf(
            Transaction(
                id[0].privateKey,
                id[0].publicKey,
                PhysicalData(
                    TemperatureData(
                        temperature = BigDecimal(r.nextDouble() * 100),
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
        assertThat(tree.root).containsExactly(*ts[0].hashId)
        assertThat(tree.collapsedTree.size).isEqualTo(1)
    }

    companion object : KLogging()


}
