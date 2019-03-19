package pt.um.lei.masb.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.fail
import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.data.TrafficFlow
import pt.um.lei.masb.blockchain.ledger.Block
import pt.um.lei.masb.blockchain.ledger.BlockParams
import pt.um.lei.masb.blockchain.ledger.MIN_DIFFICULTY
import pt.um.lei.masb.blockchain.ledger.Transaction
import pt.um.lei.masb.blockchain.ledger.emptyHash
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.ledger.truncated
import pt.um.lei.masb.blockchain.persistance.DatabaseMode
import pt.um.lei.masb.blockchain.persistance.ManagedDatabaseInfo
import pt.um.lei.masb.blockchain.persistance.ManagedSession
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.PluggableDatabase
import pt.um.lei.masb.blockchain.persistance.loaders.PreConfiguredLoaders
import pt.um.lei.masb.blockchain.service.ChainHandle
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.service.LedgerHandle
import pt.um.lei.masb.blockchain.service.LedgerService
import pt.um.lei.masb.blockchain.service.results.LoadListResult
import pt.um.lei.masb.test.utils.applyOrFail
import pt.um.lei.masb.test.utils.extractOrFail
import pt.um.lei.masb.test.utils.logActualToExpectedLists
import pt.um.lei.masb.test.utils.makeXTransactions
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

    val hash = blockChain.ledgerId.hash

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
                """Clusters present in ${database.session.name}:
                    | ${
                clusterNames.joinToString(
                    """
                        |,
                    """.trimMargin()
                ) { it }
                }
                """.trimMargin()
            }
            assertThat(
                clusterNames
            ).containsAll(
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
            assertThat(binary).containsAll(
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
                logger.info {
                    present[0].propertyNames.joinToString(
                        ", ",
                        """|
                       | Transaction properties:
                       |
                    """.trimMargin()
                    )
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
                when (transactions) {
                    is LoadListResult.Success -> {
                        logActualToExpectedLists(
                            "Transactions' hashes from DB:",
                            transactions.data.map { it.hashId.print() },
                            "Transactions' hashes from test:",
                            testTransactions.map { it.hashId.print() },
                            logger
                        )
                        assertAll {
                            assertThat(transactions.data.size).isEqualTo(testTransactions.size)
                            transactions.data.forEachIndexed { i, it ->
                                assertThat(it).isEqualTo(testTransactions[i])
                            }
                        }
                    }
                    is LoadListResult.QueryFailure ->
                        if (transactions.exception != null)
                            org.junit.jupiter.api.fail(
                                transactions.cause,
                                transactions.exception
                            )
                        else
                            fail(transactions.cause)
                    is LoadListResult.NonExistentData ->
                        fail(transactions.cause)
                    is LoadListResult.NonMatchingCrypter ->
                        fail(transactions.cause)
                    is LoadListResult.UnregisteredCrypter ->
                        fail(transactions.cause)
                    is LoadListResult.UnrecognizedDataType ->
                        fail(transactions.cause)
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
            val chain: ChainHandle = blockChain.registerNewChainHandleOf(
                TemperatureData::class.java
            ).extractOrFail()

            @Test
            fun `Test simple insertion`() {
                val block = Block(
                    blockChain.ledgerId.hash,
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
                        TrafficFlow(
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
                    TemperatureData::class.java
                ).extractOrFail()

                val block = Block(
                    blockChain.ledgerId.hash,
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
                    block.toString()
                }

            }

        }
    }


    companion object : KLogging()
}