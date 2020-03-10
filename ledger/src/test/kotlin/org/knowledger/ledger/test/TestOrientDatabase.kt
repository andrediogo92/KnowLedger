package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mapToSet
import org.knowledger.collections.toMutableSortedListFromPreSorted
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
import org.knowledger.ledger.service.transactions.*
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockUpdates
import org.knowledger.ledger.storage.block.TransactionAdding
import org.knowledger.ledger.storage.indexed
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.testing.ledger.appendByLine
import org.knowledger.testing.ledger.failOnError
import org.knowledger.testing.ledger.logActualToExpectedHashes
import org.knowledger.testing.ledger.logActualToExpectedHashing
import org.knowledger.testing.ledger.queryToList
import org.knowledger.testing.ledger.testHasher
import org.tinylog.kotlin.Logger
import kotlin.math.absoluteValue

class TestOrientDatabase {
    private val ids = arrayOf(
        Identity("test"),
        Identity("test2")
    )

    private val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    private val session = db.newManagedSession("test")
    private val hasher = testHasher
    private val trafficFlowDataStorageAdapter =
        TrafficFlowDataStorageAdapter(hasher)
    private val temperatureDataStorageAdapter =
        TemperatureDataStorageAdapter(hasher)

    private val ledger = LedgerHandle
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

    private val encoder = ledger.encoder

    private val temperatureChain: ChainHandle =
        ledger.registerNewChainHandleOf(
            temperatureDataStorageAdapter
        ).unwrap()

    private val chainId = temperatureChain.id
    private val pw = ledger.pw
    private val transactionStorageAdapter = pw.transactionStorageAdapter
    private val rawTransactions = generateXTransactions(
        id = ids, size = 20,
        hasher = hasher, encoder = encoder
    )

    @Suppress("USELESS_CAST")
    private val transactions: MutableSortedList<Transaction> =
        rawTransactions.map {
            StorageAwareTransaction(it as HashedTransactionImpl) as Transaction
        }.toMutableSortedListFromPreSorted().indexed()


    @BeforeAll
    fun `initialize DB`() {
        transactions.forEach {
            pw.persistEntity(it, transactionStorageAdapter)
        }
    }

    @Nested
    inner class Session {
        private val tid = pw.transactionStorageAdapter.id


        @Nested
        inner class Clusters {
            private val adapters = pw.defaultSchemas.also {
                it.addAll(pw.dataAdapters)
            }

            @Test
            fun `created clusters`() {
                val plug = session as OrientSession
                val clusterNames = session.clustersPresent
                Logger.debug {
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
                    Logger.debug {
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
            Logger.debug {
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
                        "hash" to transactions[0].hash.bytes
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
        private val trafficChain: ChainHandle =
            ledger.registerNewChainHandleOf(
                trafficFlowDataStorageAdapter
            ).unwrap()

        val tempBlock = generateBlockWithChain(
            transactions, temperatureChain.id,
            hasher, encoder,
            ledger.ledgerInfo.formula,
            ledger.ledgerInfo.coinbaseParams,
            ledger.ledgerInfo.ledgerParams.blockParams
        )

        private val tempQueryManager =
            pw.chainManager(trafficChain.chainHash)

        @BeforeAll
        fun `init blocks`() {
            tempBlock.coinbase.addWitnesses(transactions.toTypedArray())
            (tempBlock as BlockUpdates).newExtraNonce()

            tempQueryManager.persistEntity(
                tempBlock, tempQueryManager.blockStorageAdapter
            )
        }


        @Nested
        inner class Blocks {

            @Test
            fun `Test simple insertion`() {
                val block = generateBlockWithChain(
                    temperatureChain.id, hasher, encoder,
                    ledger.ledgerInfo.formula,
                    ledger.ledgerInfo.coinbaseParams,
                    ledger.ledgerInfo.ledgerParams.blockParams
                )
                assertThat((block as TransactionAdding) + transactions[0])
                    .isTrue()
                assertThat(block.transactions[0])
                    .isNotNull()
                    .isEqualTo(transactions[0])
            }

            @Test
            fun `Test transactions by index`() {
                tempQueryManager.getTransactionByIndex(
                    tempBlock.header.hash,
                    2
                ).mapSuccess {
                    assertThat(it).isEqualTo(transactions[2])
                }.failOnError()
            }

            @Test
            fun `Test fetch witness info`() {
                val publicKey = transactions[0].publicKey
                tempQueryManager
                    .getWitnessInfoBy(publicKey)
                    .mapSuccess { info ->
                        assertThat(info.hash).isEqualTo(tempBlock.coinbase.hash)
                        assertThat(info.index).isEqualTo(tempBlock.coinbase.findWitness(transactions[0]))
                        assertThat(info.max).isEqualTo(tempBlock.coinbase.blockheight)
                        Logger.debug { info.toString() }
                    }.failOnError()
            }

            @Test
            fun `Test transactions by time bound`() {
                val newT = generateXTransactions(ids, 1, hasher, encoder)[0]
                val current = newT.data.millis
                val currentMillis = current
                val diff = (current - transactions[0].data.millis).absoluteValue
                tempQueryManager
                    .getTransactionByBound(currentMillis, diff)
                    .mapSuccess { withBlockHash ->
                        val last = transactions.size - 1
                        assertThat(withBlockHash.txBlockHash).isEqualTo(tempBlock.header.hash)
                        assertThat(withBlockHash.txHash).isEqualTo(transactions[last].hash)
                        assertThat(withBlockHash.txIndex).isEqualTo(last)
                        assertThat(withBlockHash.txMillis).isEqualTo(transactions[last].data.millis)
                        assertThat(withBlockHash.txMin).isLessThanOrEqualTo(diff)
                        Logger.debug { withBlockHash.toString() }
                    }.failOnError()
            }

            @Test
            fun `Test traffic insertion`() {
                val testTraffic = generateXTransactions(
                    id = ids, size = 1, generator = ::trafficFlow
                )[0]

                val block = generateBlockWithChain(
                    trafficChain.id, hasher, encoder,
                    ledger.ledgerInfo.formula,
                    ledger.ledgerInfo.coinbaseParams,
                    ledger.ledgerInfo.ledgerParams.blockParams
                )
                assertThat(block).isNotNull()
                assertThat((block as TransactionAdding) + testTraffic)
                    .isTrue()
                assertThat(block.transactions[0])
                    .isNotNull()
                    .isEqualTo(testTraffic)
            }

        }
    }

    @Nested
    inner class Persistence {
        private val tempQueryManager: QueryManager =
            pw.chainManager(temperatureChain.chainHash)

        @Test
        fun `loading transactions`() {
            tempQueryManager.getTransactionsByClass(
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
            tempQueryManager.getTransactionsOrderedByTimestamp(
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
            tempQueryManager.getTransactionsFromAgent(
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
            tempQueryManager.getTransactionByHash(
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