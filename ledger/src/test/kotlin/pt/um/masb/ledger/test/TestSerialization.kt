package pt.um.masb.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.tinylog.kotlin.Logger
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.storage.Block

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

        testTransactions.forEachIndexed { i, t ->
            assertThat(block.addTransaction(t))
                .isTrue()
            assertThat(block.data[i])
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
        assertThat(rebuiltBlock).isEqualTo(block)
    }
}