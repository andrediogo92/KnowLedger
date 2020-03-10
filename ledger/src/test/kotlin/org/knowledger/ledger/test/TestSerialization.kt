package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import kotlinx.serialization.UnstableDefault
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.serial.ledgerBinarySerializer
import org.knowledger.ledger.serial.ledgerTextSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Transaction
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.testHasher
import org.tinylog.kotlin.Logger

@UnstableDefault
class TestSerialization {
    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    private val chainId = generateChainId()

    //Cache coinbase params to avoid repeated digest of formula calculations.
    private val coinbaseParams = CoinbaseParams()

    private val testSize = 10

    private val testTransactions =
        generateXTransactions(id, testSize)

    private val textSerializer = ledgerTextSerializer {
        encoder = testJson
    }.unwrap()

    private val binarySerializer = ledgerBinarySerializer {
        encoder = testEncoder
    }.unwrap()

    inline fun <T> encodeAndDecodeText(
        data: T, encode: (T) -> String, decode: (String) -> T
    ) {
        val resultingTransaction = encode(data)

        Logger.debug { resultingTransaction }
        Logger.debug {
            "Total Sequence Size -> ${resultingTransaction.length}"
        }

        val rebuiltTransaction = decode(resultingTransaction)

        assertThat(rebuiltTransaction).isEqualTo(data)
    }

    inline fun <T> encodeAndDecodeBinary(
        data: T, encode: (T) -> ByteArray, decode: (ByteArray) -> T
    ) {
        val resultingTransaction = encode(data)

        Logger.debug {
            "Total Byte Size -> ${resultingTransaction.size}"
        }

        val rebuiltTransaction = decode(resultingTransaction)

        assertThat(rebuiltTransaction).isEqualTo(data)
    }

    @Nested
    inner class Transactions {
        @Nested
        inner class Single {
            private lateinit var transaction: Transaction

            @BeforeEach
            fun startup() {
                transaction = testTransactions
                    .drop(random.randomInt(testSize))
                    .first()
            }

            @Test
            fun `serialization and deserialization of transaction by pretty print`() {
                encodeAndDecodeText(
                    transaction,
                    textSerializer::encodeTransaction,
                    textSerializer::decodeTransaction
                )
            }

            @Test
            fun `serialization and deserialization of transaction by bytes`() {
                encodeAndDecodeBinary(
                    transaction,
                    binarySerializer::encodeTransaction,
                    binarySerializer::decodeTransaction
                )
            }
        }

        @Nested
        inner class Set {
            @Test
            fun `serialization and deserialization of transaction set by pretty print`() {
                val resultingTransaction =
                    textSerializer.encodeTransactions(testTransactions)

                Logger.debug { resultingTransaction }
                Logger.debug {
                    "Total Sequence Size -> ${resultingTransaction.length}"
                }

                val rebuiltTransaction =
                    textSerializer.decodeTransactionsSet(resultingTransaction)
                assertThat(rebuiltTransaction).containsOnly(*testTransactions.toTypedArray())
            }

            @Test
            fun `serialization and deserialization of transaction set by bytes`() {
                val resultingTransaction =
                    binarySerializer.encodeTransactions(testTransactions)

                Logger.debug {
                    "Total Byte Size -> ${resultingTransaction.size}"
                }

                val rebuiltTransaction =
                    binarySerializer.decodeTransactionsSet(resultingTransaction)
                assertThat(rebuiltTransaction).containsOnly(*testTransactions.toTypedArray())
            }
        }
    }

    @Nested
    inner class MerkleTree {
        private val merkleTree = MerkleTreeImpl(testHasher, testTransactions.toTypedArray())

        @Test
        fun `serialization and deserialization of blocks by pretty print`() {
            encodeAndDecodeText(
                merkleTree,
                textSerializer::encodeMerkleTree,
                textSerializer::decodeMerkleTree
            )
        }

        @Test
        fun `serialization and deserialization of blocks by bytes`() {
            encodeAndDecodeBinary(
                merkleTree,
                binarySerializer::encodeMerkleTree,
                binarySerializer::decodeMerkleTree
            )
        }
    }

    @Nested
    inner class Blocks {
        private lateinit var block: Block

        @BeforeEach
        fun startup() {
            block = generateBlockWithChain(
                chainId = chainId, ts = testTransactions, coinbaseParams = coinbaseParams
            ).also {
                it.coinbase.addWitnesses(testTransactions.toTypedArray())
            }
        }

        @Test
        fun `serialization and deserialization of blocks by pretty print`() {
            assertThat(block.transactions.size).isEqualTo(testTransactions.size)
            encodeAndDecodeText(
                block,
                textSerializer::encodeBlock,
                textSerializer::decodeBlock
            )
        }

        @Test
        fun `serialization and deserialization of blocks by bytes`() {
            assertThat(block.transactions.size).isEqualTo(testTransactions.size)
            encodeAndDecodeBinary(
                block,
                binarySerializer::encodeBlock,
                binarySerializer::decodeBlock
            )

        }

    }


}