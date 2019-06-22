package pt.um.masb.ledger.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.tinylog.kotlin.Logger
import pt.um.masb.common.data.Difficulty.Companion.MIN_DIFFICULTY
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.results.unwrap
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TrafficFlowData
import pt.um.masb.ledger.data.adapters.TemperatureDataStorageAdapter
import pt.um.masb.ledger.data.adapters.TrafficFlowDataStorageAdapter
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.Transaction
import java.math.BigDecimal

class TestOrientDatabase {
    val ident = Identity("test")

    val testTransactions = generateXTransactions(ident, 10)

    val ledger: LedgerHandle = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrap()
        .withCustomSession(
            DatabaseMode.MEMORY, DatabaseType.MEMORY,
            null, null
        )
        .withDBPath("test")
        .build()
        .unwrap()

    val hash = ledger.ledgerId.hashId
    val trunc = hash.truncated

    val hasher: Hasher = LedgerHandle.getHasher(hash)!!

    @BeforeAll
    fun `initialize DB`() {
    }

    @Nested
    inner class TestQuerying {
        val temperatureChain: ChainHandle = ledger.registerNewChainHandleOf(
            TemperatureDataStorageAdapter
        ).unwrap()

        val trafficChain: ChainHandle = ledger.registerNewChainHandleOf(
            TrafficFlowDataStorageAdapter
        ).unwrap()


        @Nested
        inner class TestBlocks {

            @Test
            fun `Test simple insertion`() {

                val block = Block(
                    hash,
                    emptyHash,
                    MIN_DIFFICULTY,
                    1,
                    BlockParams()
                )
                assertThat(block.addTransaction(testTransactions[0]))
                    .isTrue()
                assertThat(block.data[0])
                    .isNotNull()
                    .isEqualTo(testTransactions[0])
            }


            @Test
            fun `Test traffic insertion`() {
                val testTraffic = Transaction(
                    ident.privateKey,
                    ident.publicKey,
                    PhysicalData(
                        BigDecimal.ONE,
                        BigDecimal.ONE,
                        TrafficFlowData(
                            "FRC0",
                            20,
                            20,
                            3000,
                            3000,
                            34.5,
                            12.6
                        )
                    ),
                    hasher
                )

                val block = Block(
                    hash,
                    emptyHash,
                    MIN_DIFFICULTY,
                    1,
                    BlockParams()
                )
                assertThat(block).isNotNull()
                assertThat(block.addTransaction(testTraffic))
                    .isTrue()
                assertThat(block.data[0])
                    .isNotNull()
                    .isEqualTo(testTraffic)
                Logger.info {
                    moshi.adapter(Block::class.java).toJson(block)
                }
            }

        }
    }

    fun `close database`() {
        ledger.close()
    }

}