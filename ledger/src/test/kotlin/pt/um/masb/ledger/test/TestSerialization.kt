package pt.um.masb.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import mu.KLogging
import org.junit.jupiter.api.Test
import pt.um.masb.common.data.Difficulty.Companion.MIN_DIFFICULTY
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.test.utils.makeXTransactions
import pt.um.masb.ledger.test.utils.moshi
import pt.um.masb.ledger.test.utils.randomByteArray

class TestSerialization {
    val ident = Identity("test")

    val testTransactions = makeXTransactions(ident, 4)
        .sortedByDescending {
            it.data.instant
        }

    @Test
    fun `serialization and deserialization of blocks`() {
        val block = Block(
            Hash(randomByteArray(32)),
            emptyHash,
            MIN_DIFFICULTY,
            1,
            BlockParams()
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
        logger.debug {
            resultingBlock
        }
        assertThat(json.fromJson(resultingBlock)).isEqualTo(block)
    }

    companion object : KLogging()
}