package pt.um.lei.masb.test

import mu.KLogging
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.Ident
import pt.um.lei.masb.blockchain.Transaction
import pt.um.lei.masb.blockchain.TransactionOutput
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TUnit
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.emptyHash
import pt.um.lei.masb.blockchain.print
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.SHA256Encrypter
import java.math.BigDecimal
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.util.*

class TestMerkleTree {
    private val r = SecureRandom.getInstanceStrong()

    private val id = arrayOf(
        Ident.generateNewIdent(),
        Ident.generateNewIdent()
    )

    private lateinit var tree: MerkleTree

    private lateinit var ts: List<Transaction>

    private lateinit var coinbase: Coinbase


    private val crypter: Crypter = if (Security.getProvider("BC") == null) {
        Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
        SHA256Encrypter()
    } else {
        SHA256Encrypter()
    }

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

    private fun makeXTransactions(
        id: Array<Pair<PrivateKey, PublicKey>>,
        size: Int
    ): List<Transaction> {
        val ts: MutableList<Transaction> = mutableListOf()
        for (i in 0 until size) {
            ts.add(
                Transaction(
                    id[i % 2].first,
                    id[i % 2].second,
                    PhysicalData(
                        TemperatureData(
                            temperature = BigDecimal(r.nextDouble() * 100),
                            unit = TUnit.CELSIUS
                        )
                    )
                )
            )
        }
        return ts
    }

    private fun generateCoinbase(
        id: Array<Pair<PrivateKey, PublicKey>>,
        ts: List<Transaction>
    ): Coinbase {
        val sets = listOf(
            TransactionOutput(
                id[0].second,
                emptyHash(),
                BigDecimal.ONE,
                ts[0].hashId,
                emptyHash()
            ),
            TransactionOutput(
                id[1].second,
                emptyHash(),
                BigDecimal.ONE,
                ts[1].hashId,
                emptyHash()
            )
        )
        //First transaction output has
        //transaction 0.
        //Second is transaction 2
        //referencing transaction 0.
        //Third is transaction 4
        //referencing transaction 0.
        sets[0].addToPayout(
            BigDecimal.ONE,
            ts[2].hashId,
            ts[0].hashId
        )
        sets[0].addToPayout(
            BigDecimal.ONE,
            ts[4].hashId,
            ts[0].hashId
        )
        return Coinbase(
            sets.toSet() as MutableSet<TransactionOutput>,
            BigDecimal("3"),
            emptyHash()
        )
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
            assertNotNull(tree.root)
            val nakedTree = tree.collapsedTree
            //Three levels to the left is first transaction.
            assertArrayEquals(nakedTree[7], ts[0].hashId)
            assertArrayEquals(nakedTree[8], ts[1].hashId)
            assertArrayEquals(nakedTree[9], ts[2].hashId)
            assertArrayEquals(nakedTree[10], ts[3].hashId)
            assertArrayEquals(nakedTree[11], ts[4].hashId)
            assertArrayEquals(nakedTree[12], ts[5].hashId)
            assertArrayEquals(nakedTree[13], ts[6].hashId)
            assertArrayEquals(nakedTree[14], ts[7].hashId)
            //Two levels in to the left is a hash of transaction 1 + 2.
            assertArrayEquals(
                nakedTree[3],
                crypter.applyHash(ts[0].hashId + ts[1].hashId)
            )
            assertArrayEquals(
                nakedTree[4],
                crypter.applyHash(ts[2].hashId + ts[3].hashId)
            )
            assertArrayEquals(
                nakedTree[5],
                crypter.applyHash(ts[4].hashId + ts[5].hashId)
            )
            assertArrayEquals(
                nakedTree[6],
                crypter.applyHash(ts[6].hashId + ts[7].hashId)
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertArrayEquals(
                nakedTree[1],
                crypter.applyHash(
                    crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                            crypter.applyHash(ts[2].hashId + ts[3].hashId)
                )
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertArrayEquals(
                nakedTree[2],
                crypter.applyHash(
                    crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                            crypter.applyHash(ts[6].hashId + ts[7].hashId)
                )
            )
            assertArrayEquals(
                tree.root,
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

            assertNotNull(tree.root)
            assertTrue(tree.verifyTransaction(ts[0].hashId))
            assertTrue(tree.verifyTransaction(ts[1].hashId))
            assertTrue(tree.verifyTransaction(ts[2].hashId))
            assertTrue(tree.verifyTransaction(ts[3].hashId))
            assertTrue(tree.verifyTransaction(ts[4].hashId))
            assertTrue(tree.verifyTransaction(ts[5].hashId))

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

            assertNotNull(tree.root)
            assertTrue(tree.verifyBlockTransactions(coinbase, ts))

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
            assertNotNull(tree.root)
            val nakedTree = tree.collapsedTree
            //Three levels to the left is first transaction.
            assertArrayEquals(nakedTree[6], ts[0].hashId)
            assertArrayEquals(nakedTree[7], ts[1].hashId)
            assertArrayEquals(nakedTree[8], ts[2].hashId)
            assertArrayEquals(nakedTree[9], ts[3].hashId)
            assertArrayEquals(nakedTree[10], ts[4].hashId)
            assertArrayEquals(nakedTree[11], ts[5].hashId)
            //Two levels in to the left is a hash of transaction 1 + 2.
            assertArrayEquals(
                nakedTree[3],
                crypter.applyHash(ts[0].hashId + ts[1].hashId)
            )
            assertArrayEquals(
                nakedTree[4],
                crypter.applyHash(ts[2].hashId + ts[3].hashId)
            )
            assertArrayEquals(
                nakedTree[5],
                crypter.applyHash(ts[4].hashId + ts[5].hashId)
            )
            //One level to the left is a hash of the hash of transactions 1 + 2 + hash of transactions 3 + 4
            assertArrayEquals(
                nakedTree[1],
                crypter.applyHash(
                    crypter.applyHash(ts[0].hashId + ts[1].hashId) +
                            crypter.applyHash(ts[2].hashId + ts[3].hashId)
                )
            )
            //One level to the right is a hash of the hash of transactions 1 + 2 * 2
            assertArrayEquals(
                nakedTree[2],
                crypter.applyHash(
                    crypter.applyHash(ts[4].hashId + ts[5].hashId) +
                            crypter.applyHash(ts[4].hashId + ts[5].hashId)
                )
            )
            //Root is everything else.
            assertArrayEquals(
                tree.root,
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
                assertNotNull(tree.root)
                val nakedTree = tree.collapsedTree
                //Three levels to the left is first transaction.
                //Said transaction is the coinbase
                assertArrayEquals(nakedTree[6], coinbase.hashId)
                assertArrayEquals(nakedTree[7], ts[0].hashId)
                assertArrayEquals(nakedTree[8], ts[1].hashId)
                assertArrayEquals(nakedTree[9], ts[2].hashId)
                assertArrayEquals(nakedTree[10], ts[3].hashId)
                assertArrayEquals(nakedTree[11], ts[4].hashId)
                //Two levels in to the left is a hash of transaction 1 & 2.
                assertArrayEquals(
                    nakedTree[3],
                    crypter.applyHash(coinbase.hashId + ts[0].hashId)
                )
                assertArrayEquals(
                    nakedTree[4],
                    crypter.applyHash(ts[1].hashId + ts[2].hashId)
                )
                assertArrayEquals(
                    nakedTree[5],
                    crypter.applyHash(ts[3].hashId + ts[4].hashId)
                )
                //One level to the left is a hash of the hash of transactions 1 & 2
                // + the hash of transactions 3 & 4
                assertArrayEquals(
                    nakedTree[1],
                    crypter.applyHash(
                        crypter.applyHash(coinbase.hashId + ts[0].hashId) +
                                crypter.applyHash(ts[1].hashId + ts[2].hashId)
                    )
                )
                //One level to the right is a hash of the hash of transactions 5 + 6 * 2
                assertArrayEquals(
                    nakedTree[2],
                    crypter.applyHash(
                        crypter.applyHash(ts[3].hashId + ts[4].hashId) +
                                crypter.applyHash(ts[3].hashId + ts[4].hashId)
                    )
                )
                //Root is everything else.
                assertArrayEquals(
                    tree.root,
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

            assertNotNull(tree.root)
            assertTrue(tree.verifyTransaction(ts[0].hashId))
            assertTrue(tree.verifyTransaction(ts[1].hashId))
            assertTrue(tree.verifyTransaction(ts[2].hashId))
            assertTrue(tree.verifyTransaction(ts[3].hashId))
            assertTrue(tree.verifyTransaction(ts[4].hashId))

            logger.debug {
                "Unbalanced 5-transaction tree is correct"
            }


            ts = makeXTransactions(id, 7)
            tree = MerkleTree.buildMerkleTree(ts)

            //Log constructed merkle
            logMerkle(ts, tree)


            assertNotNull(tree.root)
            assertTrue(tree.verifyTransaction(ts[0].hashId))
            assertTrue(tree.verifyTransaction(ts[1].hashId))
            assertTrue(tree.verifyTransaction(ts[2].hashId))
            assertTrue(tree.verifyTransaction(ts[3].hashId))
            assertTrue(tree.verifyTransaction(ts[4].hashId))
            assertTrue(tree.verifyTransaction(ts[5].hashId))
            assertTrue(tree.verifyTransaction(ts[6].hashId))

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

            assertNotNull(tree.root)
            assertTrue(tree.verifyBlockTransactions(coinbase, ts))

            logger.debug {
                "Unbalanced 7-transaction tree is correct"
            }

            ts = makeXTransactions(id, 5)
            coinbase = generateCoinbase(id, ts)
            tree = MerkleTree.buildMerkleTree(coinbase, ts)

            //Log constructed merkle
            logMerkle(coinbase, ts, tree)

            assertNotNull(tree.root)
            assertTrue(tree.verifyBlockTransactions(coinbase, ts))

            logger.debug {
                "Unbalanced 6-transaction tree is correct"
            }

        }

    }


    @Test
    fun `Test merkle tree creation with just root`() {
        val id = Ident.generateNewIdent()
        val r = Random()
        val ts = listOf(
            Transaction(
                id.first,
                id.second,
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

        assertNotNull(tree.root)
        //Root matches the only transaction.
        assertArrayEquals(tree.root, ts[0].hashId)
        assertEquals(1, tree.collapsedTree.size)
    }

    companion object : KLogging()


}
