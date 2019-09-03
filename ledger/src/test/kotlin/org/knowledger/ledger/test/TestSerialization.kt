package org.knowledger.ledger.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.UnstableDefault
import org.junit.jupiter.api.Test
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.storage.block.Block
import org.tinylog.kotlin.Logger

@UnstableDefault
class TestSerialization {
    private val id = arrayOf(
        Identity("test1"),
        Identity("test2")
    )

    val chainId = generateChainId()

    val testTransactions =
        generateXTransactions(id, 10).toSortedSet()

    @Test
    fun `serialization and deserialization of blocks`() {
        val block = generateBlockWithChain(
            chainId
        )

        testTransactions.forEach { t ->
            assertThat(block + t)
                .isTrue()
            assertThat(block.data.last())
                .isNotNull()
                .isEqualTo(t)
        }

        assertThat(block.data.size).isEqualTo(testTransactions.size)

        val resultingBlock = json.stringify(PolymorphicSerializer(Block::class), block)
        Logger.debug {
            resultingBlock
        }
        val rebuiltBlock = json.parse(PolymorphicSerializer(Block::class), resultingBlock) as Block
        //Even though everything seems absolutely fine
        //this blows up.
        //Deserialization is unnecessary though.
        assertThat(rebuiltBlock.coinbase).isEqualTo(block.coinbase)
        assertThat(rebuiltBlock.data.toTypedArray()).containsExactly(*block.data.toTypedArray())
        assertThat(rebuiltBlock.header).isEqualTo(block.header)
        assertThat(rebuiltBlock.merkleTree).isEqualTo(block.merkleTree)
        assertThat(rebuiltBlock).isEqualTo(block)
    }
}