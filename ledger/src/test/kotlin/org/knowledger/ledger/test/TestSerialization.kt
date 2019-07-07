package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.storage.Block
import org.tinylog.kotlin.Logger

class TestSerialization {
    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    val chainId = generateChainId()

    val testTransactions =
        generateXTransactionsWithChain(chainId, id, 10)
            .sortedByDescending {
                it.data.instant
            }

    @Test
    fun `serialization and deserialization of blocks`() {
        val block = generateBlockWithChain(
            chainId, id, testTransactions
        )

        testTransactions.forEach { t ->
            assertThat(block + t)
                .isTrue()
            assertThat(block.data.last())
                .isNotNull()
                .isEqualTo(t)
        }

        assertThat(block.data.size).isEqualTo(testTransactions.size)

        val json = moshi.adapter(Block::class.java)
        val resultingBlock = json.toJson(block)
        Logger.debug {
            resultingBlock
        }
        val rebuiltBlock = json.fromJson(resultingBlock)!!
        //Even though everything seems absolutely fine
        //this blows up.
        //Deserialization is unnecessary though.
        assertAll {
            assertThat(rebuiltBlock.coinbase).isEqualTo(block.coinbase)
            assertThat(rebuiltBlock.data.toTypedArray()).containsExactly(*block.data.toTypedArray())
            assertThat(rebuiltBlock.header).isEqualTo(block.header)
            assertThat(rebuiltBlock.merkleTree).isEqualTo(block.merkleTree)
            assertThat(rebuiltBlock).isEqualTo(block)
        }
    }
}