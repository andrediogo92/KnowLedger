package pt.um.masb.ledger.storage.transactions.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.orient.OrientSession
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerService
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import pt.um.masb.ledger.test.TestOrientDatabase
import pt.um.masb.ledger.test.utils.appendByLine
import pt.um.masb.ledger.test.utils.applyOrFail
import pt.um.masb.ledger.test.utils.extractOrFail
import pt.um.masb.ledger.test.utils.logActualToExpectedLists
import pt.um.masb.ledger.test.utils.makeXTransactions
import pt.um.masb.ledger.test.utils.testDB

class TestPersistence {
    val database: ManagedDatabase = testDB()
    val session: ManagedSession = database.newManagedSession()
    val pw = PersistenceWrapper(session)
    val ident = Identity("test")

    val testTransactions = makeXTransactions(ident, 10)

    val ledger = LedgerService(database)
        .newLedgerHandle("test")
        .extractOrFail()

    val hash = ledger.ledgerId.hashId

    val trunc = hash.truncated

    val transactionStorageAdapter = TransactionStorageAdapter()

    @BeforeAll
    fun `initialize DB and transactions`() {
        session.makeActive()

        testTransactions.forEach {
            pw.persistEntity(
                it,
                transactionStorageAdapter,
                "${transactionStorageAdapter.id.toLowerCase()}${trunc.toLowerCase()}"
            )
        }
    }

    init {
    }


    @Nested
    inner class TestClusters {
        @Test
        fun `Test created clusters`() {
            val plug = (session as OrientSession)
            val clusterNames = plug.clustersPresent
            TestOrientDatabase.logger.info {
                StringBuilder()
                    .append("Clusters present in ${plug.name}")
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
            session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }",
                emptyMap()
            ).let { set ->
                val l = set.asSequence().toList()
                l.forEach { res ->
                    TestOrientDatabase.logger.info {
                        res.element.print()
                    }
                }
                assertAll {
                    assertThat(l.size).isEqualTo(testTransactions.size)
                    l.map {
                        it.element.getHashProperty("hashId")
                    }.forEachIndexed { i, hash ->
                        assertThat(hash.print).isEqualTo(
                            testTransactions[i].hashId.print
                        )
                    }
                }

            }
        }

        @Test
        fun `Test binary records`() {
            val binary = session.query(
                "select from cluster:transaction${
                trunc.toLowerCase()
                }",
                emptyMap()
            ).next().element.getHashProperty("hashId")
            assertThat(binary.bytes).containsExactly(
                *testTransactions[0].hashId.bytes
            )
        }

    }


    @Nested
    inner class TestTransactions {
        @Test
        fun `Test simple insertion`() {
            val orient = (session as OrientSession)
            val present = orient.browseClass(
                "Transaction"
            ).toList()
            val schemaProps = TransactionStorageAdapter().properties.keys.toTypedArray()
            assertThat(
                present[0].presentProperties
            ).isNotNull().containsAll(
                *schemaProps
            )
            TestOrientDatabase.logger.info {
                StringBuilder("Properties in Transaction:")
                    .appendByLine(present[0].presentProperties)
                    .toString()
            }
            logActualToExpectedLists(
                "Transactions' hashes from DB:",
                present.map {
                    it.getHashProperty("hashId").print
                },
                "Transactions' hashes from test:",
                testTransactions.map { it.hashId.print },
                TestOrientDatabase.logger
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
                    this.map { it.hashId.print },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print },
                    TestOrientDatabase.logger
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
                    this.map { it.hashId.print },
                    "Transactions' hashes from test:",
                    reversed.map { it.hashId.print },
                    TestOrientDatabase.logger
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
                    this.map { it.hashId.print },
                    "Transactions' hashes from test:",
                    testTransactions.map { it.hashId.print },
                    TestOrientDatabase.logger
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

    @AfterAll
    fun `close database session`() {
        session.close()
        database.close()
    }
}