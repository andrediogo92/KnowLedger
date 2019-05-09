package pt.um.lei.masb.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.lei.masb.test.utils.appendByLine
import pt.um.lei.masb.test.utils.applyOrFail
import pt.um.lei.masb.test.utils.extractOrFail
import pt.um.lei.masb.test.utils.logActualToExpectedLists
import pt.um.lei.masb.test.utils.makeXTransactions
import pt.um.lei.masb.test.utils.moshi
import pt.um.masb.common.MIN_DIFFICULTY
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.ManagedDatabaseInfo
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.PluggableDatabase
import pt.um.masb.common.emptyHash
import pt.um.masb.common.print
import pt.um.masb.common.truncated
import pt.um.masb.ledger.Block
import pt.um.masb.ledger.Transaction
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TemperatureData
import pt.um.masb.ledger.data.TrafficFlowData
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.Ident
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.LedgerService
import pt.um.masb.ledger.storage.loaders.PreConfiguredLoaders
import pt.um.masb.ledger.storage.schema.PreConfiguredSchemas
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.math.BigDecimal

class TestDatabase {
    val database: ManagedSession = PluggableDatabase(
        ManagedDatabaseInfo(
            modeOpen = DatabaseMode.MEMORY,
            path = "./test",
            mode = ODatabaseType.MEMORY
        )
    ).newManagedSession()

    internal val pw = PersistenceWrapper(database)

    val session = pw.getInstanceSession()

    val ident = Ident("test")

    val testTransactions = makeXTransactions(ident, 10)

    val blockChain = LedgerHandle(pw, "test")

    val hash = blockChain.ledgerId.hashId

    val trunc = hash.truncated()

    init {
        testTransactions.forEach {
            it.store(session).save<OElement>(
                "transaction${trunc.toLowerCase()}"
            )
        }
    }


    @Nested
    inner class TestClusters {
        @Test
        fun `Test created clusters`() {
            val clusterNames = database.session.clusterNames
            logger.info {
                StringBuilder()
                    .append("Clusters present in ${database.session.name}")
                    .appendByLine(clusterNames)
                    .toString()
            }
            assertThat(
                clusterNames
            ).containsAll(
                "BlockPool$trunc".toLowerCase(),
                "TransactionPool$trunc".toLowerCase(),
                "Transaction$trunc".toLowerCase(),
                "ChainHandle$trunc".toLowerCase(),
                "TransactionOutput$trunc".toLowerCase(),
                "Coinbase$trunc".toLowerCase(),
                "Block$trunc".toLowerCase(),
                "LedgerId$trunc".toLowerCase(),
                "BlockHeader$trunc".toLowerCase(),
                "PhysicalData$trunc".toLowerCase()
            )
        }

        @Test
        fun `Test cluster query`() {
            database.session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }"
            ).let { set ->
                val l = set.asSequence().toList()
                l.forEach { res ->
                    logger.info {
                        res.toJSON()
                    }
                }
                assertAll {
                    assertThat(l.size).isEqualTo(testTransactions.size)
                    l.map {
                        it.toElement().getProperty<ByteArray>(
                            "hashId"
                        )
                    }.forEachIndexed { i, hash ->
                        assertThat(hash.print()).isEqualTo(
                            testTransactions[i].hashId.print()
                        )
                    }
                }

            }
        }

        @Test
        fun `Test binary records`() {
            val binary = database.session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }"
            ).next().toElement().getProperty<ByteArray>(
                "hashId"
            )
            assertThat(binary).containsExactly(
                *testTransactions[0].hashId
            )
        }

    }

    @Nested
    inner class TestQuerying {

        init {
            LedgerService.registerLoaders(
                hash,
                PreConfiguredLoaders
            )
        }

        @Nested
        inner class TestTransactions {
            @Test
            fun `Test simple insertion`() {
                val present = database.session.browseClass(
                    "Transaction"
                ).toList()
                val schemaProps = PreConfiguredSchemas.chainSchemas.find {
                    it.id == "Transaction"
                }?.properties?.keys?.toTypedArray() ?: emptyArray()
                assertThat(
                    present[0].propertyNames
                ).isNotNull().containsAll(
                    *schemaProps
                )
                logger.info {
                    StringBuilder("Properties in Transaction:")
                        .appendByLine(present[0].propertyNames)
                        .toString()
                }
                logActualToExpectedLists(
                    "Transactions' hashes from DB:",
                    present.map {
                        it.getProperty<ByteArray>("hashId").print()
                    },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print() },
                    logger
                )
                assertThat(
                    present.size
                ).isEqualTo(10)
            }

            @Test
            fun `Test loading transactions`() {
                val transactions = pw.getTransactionsByClass(
                    hash,
                    "Temperature"
                )
                transactions.applyOrFail {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print() },
                        "Transactions' hashes from test:",
                        testTransactions.map { it.hashId.print() },
                        logger
                    )
                    assertAll {
                        assertThat(this.size).isEqualTo(testTransactions.size)
                        this.forEachIndexed { i, it ->
                            assertThat(it).isEqualTo(testTransactions[i])
                        }
                    }
                }
            }

            @Test
            fun `Test loading by timestamp`() {
                pw.getTransactionsOrderedByTimestamp(
                    hash
                ).applyOrFail {
                    val reversed = testTransactions.asReversed()
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print() },
                        "Transactions' hashes from test:",
                        reversed.map { it.hashId.print() },
                        logger
                    )
                    assertAll {
                        assertThat(this.size).isEqualTo(
                            testTransactions.size
                        )
                        this.forEachIndexed { i, it ->
                            assertThat(it).isEqualTo(reversed[i])
                        }
                    }
                }

            }

            @Test
            fun `Test loading by Public Key`() {
                pw.getTransactionsFromAgent(
                    hash,
                    ident.publicKey
                ).applyOrFail {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print() },
                        "Transactions' hashes from test:",
                        testTransactions.map { it.hashId.print() },
                        logger
                    )
                    assertAll {
                        assertThat(this.size).isEqualTo(testTransactions.size)
                        this.forEachIndexed { i, it ->
                            assertThat(it).isEqualTo(testTransactions[i])
                        }
                    }
                }
            }

            @Test
            fun `Test loading by hash`() {
                pw.getTransactionByHash(
                    hash,
                    testTransactions[2].hashId
                ).applyOrFail {
                    assertThat(this)
                        .isNotNull()
                        .isEqualTo(testTransactions[2])
                }
            }

        }

        @Nested
        inner class TestBlocks {

            @Test
            fun `Test simple insertion`() {
                val chain: ChainHandle = blockChain.registerNewChainHandleOf(
                    TemperatureData::class.java
                ).extractOrFail()

                val block = Block(
                    hash,
                    emptyHash(),
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
                    )
                )
                val sidechain: ChainHandle = blockChain.registerNewChainHandleOf(
                    TrafficFlowData::class.java
                ).extractOrFail()

                val block = Block(
                    hash,
                    emptyHash(),
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
                logger.info {
                    moshi.adapter(Block::class.java).toJson(block)
                }
            }

        }
    }


    companion object : KLogging()
}