package org.knowledger.ledger.storage.test

import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.collections.SortedList
import org.knowledger.collections.toSortedListFromPreSorted
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.immutableCopy
import org.knowledger.ledger.results.unwrapFailure
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.immutableCopy
import org.knowledger.ledger.storage.serial.ledgerBinarySerializer
import org.knowledger.ledger.storage.serial.ledgerTextSerializer
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.RandomDataSchema
import org.knowledger.testing.logging.encodeAndDecodeBinary
import org.knowledger.testing.logging.encodeAndDecodeText
import org.knowledger.testing.storage.defaultCbor
import org.knowledger.testing.storage.defaultFactories
import org.knowledger.testing.storage.defaultJson
import org.knowledger.testing.storage.generateBlockWithChain
import org.knowledger.testing.storage.generateChainId
import org.knowledger.testing.storage.generateCoinbaseParams
import org.knowledger.testing.storage.generateXTransactions

class TestSerialization {
    private val id = arrayOf(Identity("test1"), Identity("test2"))

    //Cache coinbase params to avoid repeated digest of formula calculations.
    private val coinbaseParams = generateCoinbaseParams()

    private val chainId = generateChainId(
        ledgerHash = random.randomHash(), adapter = RandomDataSchema(),
        coinbaseParams = coinbaseParams
    )


    private val testSize = 10

    private val testTransactions = generateXTransactions(id, testSize)

    private val textSerializer = ledgerTextSerializer {
        encoder = defaultJson
    }.unwrapFailure()

    private val binarySerializer = ledgerBinarySerializer {
        encoder = defaultCbor
    }.unwrapFailure()

    @Nested
    inner class Transactions {
        @Nested
        inner class Single {
            private lateinit var transaction: ImmutableTransaction

            @BeforeEach
            fun startup() {
                transaction = testTransactions[random.randomInt(testSize)].immutableCopy()
            }

            @Test
            fun `serialization and deserialization of transaction by pretty print`() {
                val (rebuilt, _) = encodeAndDecodeText(
                    transaction, textSerializer::encodeTransaction,
                    textSerializer::decodeTransaction
                )
                assertThat(rebuilt).isEqualTo(transaction)
            }

            @Test
            fun `serialization and deserialization of transaction by bytes`() {
                val (rebuilt, _) = encodeAndDecodeBinary(
                    transaction, binarySerializer::encodeTransaction,
                    binarySerializer::decodeTransaction
                )
                assertThat(rebuilt).isEqualTo(transaction)
            }
        }

        @Nested
        inner class Set {
            private val transactions: SortedList<ImmutableTransaction> =
                testTransactions.map(Transaction::immutableCopy)
                    .toSortedListFromPreSorted()

            @Test
            fun `serialization and deserialization of transaction set by pretty print`() {
                val (rebuilt, _) = encodeAndDecodeText(
                    transactions, textSerializer::encodeTransactions,
                    textSerializer::decodeTransactionsSorted
                )

                assertThat(rebuilt).containsOnly(*transactions.toTypedArray())
            }

            @Test
            fun `serialization and deserialization of transaction set by bytes`() {
                val (rebuilt, _) = encodeAndDecodeBinary(
                    transactions, binarySerializer::encodeTransactions,
                    binarySerializer::decodeTransactionsSorted
                )

                assertThat(rebuilt).containsOnly(*transactions.toTypedArray())
            }
        }
    }

    @Nested
    inner class MerkleTree {
        private val merkleTree = defaultFactories.merkleTreeFactory.create(
            defaultHasher, testTransactions.toTypedArray()
        ).immutableCopy()

        @Test
        fun `serialization and deserialization of merkle tree by pretty print`() {
            val (rebuilt, _) = encodeAndDecodeText(
                merkleTree, textSerializer::encodeMerkleTree,
                textSerializer::decodeMerkleTree
            )
            assertThat(rebuilt).isEqualTo(merkleTree)
        }

        @Test
        fun `serialization and deserialization of merkle tree by bytes`() {
            val (rebuilt, _) = encodeAndDecodeBinary(
                merkleTree, binarySerializer::encodeMerkleTree,
                binarySerializer::decodeMerkleTree
            )
            assertThat(rebuilt).isEqualTo(merkleTree)
        }
    }

    @Nested
    inner class Blocks {
        private val block: ImmutableBlock =
            generateBlockWithChain(testTransactions, chainId).immutableCopy()


        @Test
        fun `serialization and deserialization of blocks by pretty print`() {
            assertThat(block.transactions.size).isEqualTo(testTransactions.size)
            val (rebuilt, _) = encodeAndDecodeText(
                block, textSerializer::encodeBlock,
                textSerializer::decodeBlock
            )
            assertThat(rebuilt).isEqualTo(block)
        }

        @Test
        fun `serialization and deserialization of blocks by bytes`() {
            assertThat(block.transactions.size).isEqualTo(testTransactions.size)
            val (rebuilt, _) = encodeAndDecodeBinary(
                block, binarySerializer::encodeBlock,
                binarySerializer::decodeBlock
            )
            assertThat(rebuilt).isEqualTo(block)
        }

    }


}