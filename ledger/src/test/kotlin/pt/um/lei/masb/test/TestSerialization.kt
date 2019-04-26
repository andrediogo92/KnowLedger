package pt.um.lei.masb.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import mu.KLogging
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.ledger.Block
import pt.um.lei.masb.blockchain.ledger.MIN_DIFFICULTY
import pt.um.lei.masb.blockchain.ledger.config.BlockParams
import pt.um.lei.masb.blockchain.ledger.emptyHash
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.test.utils.makeXTransactions
import pt.um.lei.masb.test.utils.moshi
import pt.um.lei.masb.test.utils.randomByteArray

class TestSerialization {
    val ident = Ident("test")

    val testTransactions = makeXTransactions(ident, 4)
        .sortedByDescending {
            it.data.instant
        }

    @Test
    fun `serialization and deserialization of blocks`() {
        val block = Block(
            randomByteArray(32),
            emptyHash(),
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