package pt.um.masb.ledger.storage.transactions.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import mu.KLogging
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.database.orient.OrientSession
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.ledger.data.adapters.TemperatureDataStorageAdapter
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import pt.um.masb.ledger.test.appendByLine
import pt.um.masb.ledger.test.failOnLoadError
import pt.um.masb.ledger.test.generateXTransactions
import pt.um.masb.ledger.test.logActualToExpectedLists

class TestPersistence {
    val database: ManagedDatabase = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            path = "",
            databaseType = DatabaseType.MEMORY
        )
    )

    val session: ManagedSession =
        database.newManagedSession("test")
    val pw = PersistenceWrapper(session)
    val ident = Identity("test")

    val ledgerHandle =
        LedgerHandle
            .Builder()
            .withCustomDB(database, session, pw)
            .withLedgerIdentity("test")
            .unwrap()
            .build()
            .unwrap()

    val hash = ledgerHandle.ledgerId.hashId
    val testTransactions = generateXTransactions(ident, 25)


    @BeforeAll
    fun `initialize DB and transactions`() {
        session.makeActive()

        logger.info {
            """LedgerHash is ${hash.print}
                | Base64: ${base64Encode(hash)}
            """.trimMargin()
        }

        assert(
            ledgerHandle.addStorageAdapter(TemperatureDataStorageAdapter)
        )

        assert(
            LedgerHandle.getContainer(hash) != null
        )

        testTransactions.forEach {
            pw.persistEntity(
                it,
                TransactionStorageAdapter
            ).mapError {
                when (this.failure) {
                    is QueryFailure.UnknownFailure ->
                        (this.failure as QueryFailure.UnknownFailure)
                            .exception
                            ?.let { exception ->
                                throw exception
                            } ?: logger.error {
                            this.failure.cause
                        }
                    else ->
                        logger.error {
                            this.failure.cause
                        }
                }
                this
            }
        }

    }


    @Nested
    inner class TestClusters {
        @Test
        fun `Test created clusters`() {
            val plug = (session as OrientSession)
            val clusterNames = plug.clustersPresent
            logger.info {
                StringBuilder()
                    .append("Clusters present in ${plug.name}")
                    .appendByLine(clusterNames)
                    .toString()
            }
            assertThat(
                clusterNames.asSequence().map {
                    it.substringBefore('_')
                }.toSet()
            ).containsAll(
                "Transaction".toLowerCase(),
                "ChainHandle".toLowerCase(),
                "TransactionOutput".toLowerCase(),
                "Coinbase".toLowerCase(),
                "Block".toLowerCase(),
                "LedgerId".toLowerCase(),
                "BlockHeader".toLowerCase(),
                "PhysicalData".toLowerCase()
            )
        }

        @Test
        fun `Test cluster query`() {
            //Query from first cluster.
            session.query(
                "select from cluster:transaction_1",
                emptyMap()
            ).let { set ->
                val l = set.asSequence().toList()
                l.forEach { res ->
                    logger.info {
                        res.element.print()
                    }
                }
                //Ensure there is a subset of the generated transactions
                //present.
                assertAll {
                    assertThat(l.size).isLessThanOrEqualTo(testTransactions.size)
                    l.map {
                        it.element.getHashProperty("hashId")
                    }.forEachIndexed { i, hash ->
                        assertThat(testTransactions.map {
                            it.hashId
                        }).contains(
                            hash
                        )
                    }
                }

            }
        }

        @Test
        fun `Test binary records`() {
            val t = session.query(
                "select from transaction"
            )
            assertThat(t.hasNext()).isTrue()
            val binary = t.next().element.getHashProperty("hashId")
            assertThat(binary.bytes).containsExactly(
                *testTransactions[0].hashId.bytes
            )
        }

    }


    @Nested
    inner class TestTransactions {
        @Test
        fun `Test insertion successful all properties present`() {
            val orient = (session as OrientSession)
            val present = orient.browseClass(
                TransactionStorageAdapter.id
            ).toList()
            assertThat(present.size).isEqualTo(testTransactions.size)
            val schemaProps =
                TransactionStorageAdapter.properties.keys.toTypedArray()
            for (p in present) {
                assertThat(
                    p.presentProperties
                ).isNotNull().containsAll(
                    *schemaProps
                )
            }
            logger.info {
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
                logger
            )
        }

        @Test
        fun `Test loading transactions`() {
            pw.getTransactionsByClass(
                hash,
                TemperatureDataStorageAdapter.id
            ).flatMapSuccess {
                this.toList().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print },
                        "Transactions' hashes from test:",
                        testTransactions.map { it.hashId.print },
                        logger
                    )
                    assertThat(this.size).isEqualTo(
                        testTransactions.size
                    )
                    assertThat(this).containsOnly(
                        *testTransactions.toTypedArray()
                    )
                }

            }.failOnLoadError()
        }

        @Test
        fun `Test loading by timestamp`() {
            pw.getTransactionsOrderedByTimestamp(
                hash
            ).flatMapSuccess {
                this.toList().apply {
                    val reversed = testTransactions.asReversed()
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print },
                        "Transactions' hashes from test:",
                        reversed.map { it.hashId.print },
                        logger
                    )
                    assertThat(this.size).isEqualTo(
                        testTransactions.size
                    )
                    assertThat(this).containsExactly(
                        *testTransactions.asReversed().toTypedArray()
                    )

                }
            }.failOnLoadError()

        }

        @Test
        fun `Test loading by Public Key`() {
            pw.getTransactionsFromAgent(
                hash, ident.publicKey
            ).flatMapSuccess {
                this.toList().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        this.map { it.hashId.print },
                        "Transactions' hashes from test:",
                        testTransactions.map { it.hashId.print },
                        logger
                    )
                    assertThat(this.size).isEqualTo(
                        testTransactions.size
                    )
                    assertThat(this).containsOnly(
                        *testTransactions.toTypedArray()
                    )
                }
            }.failOnLoadError()
        }

        @Test
        fun `Test loading by hash`() {
            pw.getTransactionByHash(
                hash,
                testTransactions[2].hashId
            ).flatMapSuccess {
                assertThat(this)
                    .isNotNull()
                    .isEqualTo(testTransactions[2])
            }.failOnLoadError()
        }

    }

    @AfterAll
    fun `close database session`() {
        session.close()
        database.close()
    }

    companion object : KLogging()
}