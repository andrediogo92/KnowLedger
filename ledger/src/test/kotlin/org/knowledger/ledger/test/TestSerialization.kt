package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlinx.serialization.UnstableDefault
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.serial.BlockSerializer
import org.knowledger.ledger.serial.internal.BlockByteSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.testing.ledger.testEncoder
import org.knowledger.testing.ledger.testJson
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

    private val testTransactions =
        generateXTransactions(id, 10).toSortedSet()


    @Nested
    inner class Blocks {
        lateinit var block: Block

        @BeforeEach
        fun startup() {
            block = generateBlockWithChain(
                chainId = chainId, coinbaseParams = coinbaseParams
            )
        }

        @Test
        fun `serialization and deserialization of blocks by pretty print`() {
            testTransactions.forEach { t ->
                block + t
            }

            assertThat(block.transactions.size).isEqualTo(testTransactions.size)

            val resultingBlock = testJson.stringify(BlockSerializer, block)
            Logger.debug {
                resultingBlock
            }
            val rebuiltBlock = testJson.parse(BlockSerializer, resultingBlock)
            assertThat(rebuiltBlock.coinbase).isEqualTo(block.coinbase)
            assertThat(rebuiltBlock.transactions.toTypedArray()).containsExactly(*block.transactions.toTypedArray())
            assertThat(rebuiltBlock.header).isEqualTo(block.header)
            assertThat(rebuiltBlock.merkleTree).isEqualTo(block.merkleTree)
        }

        @Test
        fun `serialization and deserialization of blocks by bytes`() {
            testTransactions.forEach { t ->
                block + t
            }

            assertThat(block.transactions.size).isEqualTo(testTransactions.size)

            val resultingBlock = testEncoder.dump(BlockByteSerializer, block)
            val rebuiltBlock = testEncoder.load(BlockByteSerializer, resultingBlock)
            //Even though everything seems absolutely fine
            //this blows up.
            //Deserialization is unnecessary though.
            assertThat(rebuiltBlock.coinbase).isEqualTo(block.coinbase)
            assertThat(rebuiltBlock.transactions.toTypedArray()).containsExactly(*block.transactions.toTypedArray())
            assertThat(rebuiltBlock.header).isEqualTo(block.header)
            assertThat(rebuiltBlock.merkleTree).isEqualTo(block.merkleTree)
        }

    }


}