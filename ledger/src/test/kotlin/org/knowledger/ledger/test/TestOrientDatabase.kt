package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.collections.mapToSet
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.truncatedHexString
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.data.adapters.TrafficFlowDataStorageAdapter
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.orient.OrientDatabase
import org.knowledger.ledger.database.orient.OrientDatabaseInfo
import org.knowledger.ledger.database.orient.OrientSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.getTransactionByHash
import org.knowledger.ledger.service.transactions.getTransactionsByClass
import org.knowledger.ledger.service.transactions.getTransactionsFromAgent
import org.knowledger.ledger.service.transactions.getTransactionsOrderedByTimestamp
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.testing.ledger.appendByLine
import org.knowledger.testing.ledger.failOnError
import org.knowledger.testing.ledger.logActualToExpectedHashes
import org.knowledger.testing.ledger.logActualToExpectedHashing
import org.knowledger.testing.ledger.queryToList
import org.knowledger.testing.ledger.testHasher
import org.tinylog.kotlin.Logger

class TestOrientDatabase {
    val ids = arrayOf(Identity("test"), Identity("test2"))

    val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    val session = db.newManagedSession("test")
    val hasher = testHasher
    val trafficFlowDataStorageAdapter = TrafficFlowDataStorageAdapter(hasher)
    val temperatureDataStorageAdapter = TemperatureDataStorageAdapter(hasher)

    val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrap()
        .withCustomDB(db, session)
        .withHasher(hasher)
        .withCustomParams(
            LedgerParams(
                hasher.id,
                blockParams = BlockParams(
                    blockLength = 20,
                    blockMemorySize = 500000
                )
            )
        )
        .withTypeStorageAdapters(
            temperatureDataStorageAdapter,
            trafficFlowDataStorageAdapter
        )
        .build()
        .unwrap()

    val encoder = ledger.encoder
    val hash = ledger.ledgerHash

    val temperatureChain: ChainHandle =
        ledger.registerNewChainHandleOf(
            temperatureDataStorageAdapter
        ).unwrap()

    val chainId = temperatureChain.id
    val chainHash = temperatureChain.id.hash
    private val pw = ledger.pw
    private val transactionStorageAdapter = pw.transactionStorageAdapter
    val transactions = generateXTransactionsArray(
        id = ids, size = 20,
        hasher = hasher, encoder = encoder
    ).toSortedSet()


    @BeforeAll
    fun `initialize DB`() {
        transactions.forEach {
            pw.persistEntity(it, transactionStorageAdapter)
        }
    }

    @Nested
    inner class Session {
        val tid = pw.transactionStorageAdapter.id


        @Nested
        inner class Clusters {
            val adapters = pw.defaultSchemas.also {
                it.addAll(pw.dataAdapters)
            }

            @Test
            fun `created clusters`() {
                val plug = session as OrientSession
                val clusterNames = session.clustersPresent
                Logger.info {
                    StringBuilder()
                        .append(System.lineSeparator())
                        .append("Clusters present in ${plug.name}")
                        .appendByLine(clusterNames)
                        .toString()
                }
                assertThat(
                    clusterNames.mapToSet {
                        it.substringBeforeLast('_')
                    }
                ).containsAll(
                    *adapters.map {
                        it.id.toLowerCase()
                    }.toTypedArray()
                )
            }

            @Test
            fun `cluster query`() {
                //Query from first cluster.
                val elements = session.queryToList(
                    """
                        SELECT 
                        FROM CLUSTER:${tid}_1
                    """.trimIndent()
                )
                elements.forEach { res ->
                    Logger.info {
                        res.json
                    }
                }
                //Ensure there is a subset of the generated transactions
                //present.
                assertAll {
                    assertThat(elements.size).isLessThanOrEqualTo(transactions.size)
                    elements.map {
                        it.getHashProperty("hash")
                    }.forEach { hash ->
                        assertThat(transactions.map {
                            it.hash
                        }).contains(
                            hash
                        )
                    }
                }

            }

        }

        @Test
        fun `transaction all properties present`() {
            val orient = (session as OrientSession)
            val present = orient.browseClass(
                tid
            ).toList()
            assertThat(present.size).isEqualTo(transactions.size)
            val schemaProps =
                transactionStorageAdapter.properties.keys.toTypedArray()
            assertAll {
                present.forEach {
                    assertThat(
                        it.presentProperties
                    ).isNotNull().containsAll(
                        *schemaProps
                    )
                }
            }
            Logger.info {
                StringBuilder("Properties in Transaction:")
                    .appendByLine(present[0].presentProperties)
                    .toString()
            }
            logActualToExpectedHashes(
                "Transactions' hashes from DB:",
                present.map {
                    it.getHashProperty("hash")
                },
                "Transactions' hashes from test:",
                transactions.map { it.hash }
            )
        }

        @Test
        fun `binary records`() {
            val elements = session.queryToList(
                """
                    SELECT 
                    FROM $tid
                """.trimIndent()
            )
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(
                *transactions.first().hash.bytes
            )
        }

        @Test
        fun `transaction with hash id`() {
            val elements = session.queryToList(
                UnspecificQuery(
                    """
                    SELECT
                    FROM $tid
                    WHERE hash = :hash
                    """.trimIndent(),
                    mapOf(
                        "hash" to transactions.first().hash.bytes
                    )
                )
            )
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(
                *transactions.first().hash.bytes
            )

        }
    }

    @Nested
    inner class Handles {
        val trafficChain: ChainHandle = ledger.registerNewChainHandleOf(
            trafficFlowDataStorageAdapter
        ).unwrap()

        @Nested
        inner class Blocks {

            @Test
            fun `Test simple insertion`() {

                val block = generateBlockWithChain(
                    temperatureChain.id, hasher, encoder, ledger.container.formula,
                    ledger.container.coinbaseParams,
                    ledger.container.ledgerParams.blockParams
                )
                assertThat(block + transactions.first())
                    .isTrue()
                assertThat(block.transactions.first())
                    .isNotNull()
                    .isEqualTo(transactions.first())
            }


            @Test
            fun `Test traffic insertion`() {
                val testTraffic = generateXTransactions(
                    id = ids, size = 1, generator = ::trafficFlow
                )[0]

                val block = generateBlockWithChain(
                    trafficChain.id, hasher, encoder, ledger.container.formula,
                    ledger.container.coinbaseParams,
                    ledger.container.ledgerParams.blockParams
                )
                assertThat(block).isNotNull()
                assertThat(block + testTraffic)
                    .isTrue()
                assertThat(block.transactions.first())
                    .isNotNull()
                    .isEqualTo(testTraffic)
            }

        }
    }

    @Nested
    inner class Persistence {

        @Test
        fun `loading transactions`() {
            pw.getTransactionsByClass(
                chainId.tag
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedHashing(
                        "Transactions' hashes from DB:",
                        this,
                        "Transactions' hashes from test:",
                        transactions
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsOnly(
                        *transactions.toTypedArray()
                    )
                }
            }.failOnError()
        }

        @Test
        fun `loading transactions by timestamp`() {
            pw.getTransactionsOrderedByTimestamp(
                chainId.tag
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedHashing(
                        "Transactions' hashes from DB:",
                        this,
                        "Transactions' hashes from test:",
                        transactions
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsExactly(
                        *transactions.toTypedArray()
                    )

                }
            }.failOnError()

        }

        @Test
        fun `loading transactions by Public Key`() {
            val key = ids[0].publicKey
            val expected = transactions.filter { it.publicKey == key }
            pw.getTransactionsFromAgent(
                chainId.tag, key
            ).mapSuccess { seq ->
                seq.asTransactions().apply {
                    logActualToExpectedHashing(
                        "Transactions' hashes from DB from ${key.truncatedHexString()}:",
                        this,
                        "Transactions' hashes from test from ${key.truncatedHexString()}:",
                        expected
                    )
                    assertThat(size).isEqualTo(
                        expected.size
                    )
                    assertThat(this).containsOnly(
                        *expected.toTypedArray()
                    )
                }
            }.failOnError()
        }

        @Test
        fun `loading transaction by hash`() {
            pw.getTransactionByHash(
                chainId.tag,
                transactions.elementAt(2).hash
            ).mapSuccess {
                assertThat((it as StorageAwareTransaction).transaction)
                    .isNotNull()
                    .isEqualTo(transactions.elementAt(2))
            }.failOnError()
        }
    }

    @AfterAll
    fun `close database`() {
        ledger.close()
    }

}