package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.isEqualTo
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.github.michaelbull.result.map
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.collections.mapToSet
import org.knowledger.database.orient.OrientDatabase
import org.knowledger.database.orient.OrientDatabaseInfo
import org.knowledger.database.orient.OrientSession
import org.knowledger.encoding.base64.truncatedBase64Encoded
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.chain.handles.LedgerHandle
import org.knowledger.ledger.chain.service.LedgerConfigurationService
import org.knowledger.ledger.chain.transactions.QueryManager
import org.knowledger.ledger.chain.transactions.getTransactionByBound
import org.knowledger.ledger.chain.transactions.getTransactionByHash
import org.knowledger.ledger.chain.transactions.getTransactionByIndex
import org.knowledger.ledger.chain.transactions.getTransactionsByClass
import org.knowledger.ledger.chain.transactions.getTransactionsFromAgent
import org.knowledger.ledger.chain.transactions.getTransactionsOrderedByTimestamp
import org.knowledger.ledger.chain.transactions.getWitnessInfoBy
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.data.adapters.TrafficFlowDataStorageAdapter
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.unwrapFailure
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.immutableCopy
import org.knowledger.ledger.storage.saFactories
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.appendByLine
import org.knowledger.testing.ledger.failOnError
import org.knowledger.testing.ledger.logActualToExpectedHashes
import org.knowledger.testing.ledger.logActualToExpectedHashing
import org.knowledger.testing.ledger.queryToList
import org.knowledger.testing.storage.generateBlockWithChain
import org.knowledger.testing.storage.generateLedgerParams
import org.knowledger.testing.storage.generateXTransactions
import org.tinylog.kotlin.Logger
import kotlin.math.absoluteValue

class TestOrientDatabase {
    private val ids = arrayOf(Identity("test"), Identity("test2"))

    private val db = OrientDatabase(
        OrientDatabaseInfo(DatabaseMode.MEMORY, DatabaseType.MEMORY, "test")
    )
    private val session = db.newManagedSession("test")
    private val hashers = defaultHasher
    private val factories: Factories = saFactories
    private val trafficFlowDataStorageAdapter = TrafficFlowDataStorageAdapter(hashers)
    private val temperatureDataStorageAdapter = TemperatureDataStorageAdapter(hashers)

    val config = LedgerConfigurationService.also { service ->
        service.registerAdapter(TemperatureDataStorageAdapter::class)
        service.registerAdapter(TrafficFlowDataStorageAdapter::class)
    }

    private val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrapFailure()
        .withCustomDB(db, session)
        .withHasher(hashers)
        .withCustomParams(generateLedgerParams().immutableCopy())
        .build()
        .unwrapFailure()

    private val encoder = ledger.encoder

    private val temperatureChain: ChainHandle =
        ledger.registerNewChainHandleOf(temperatureDataStorageAdapter).unwrapFailure()

    private val chainId = temperatureChain.chainId
    private val pw = ledger.pw
    private val transactionStorageAdapter =
        LedgerConfigurationService.ledgerAdapters.transactionStorageAdapter
    private val transactions = generateXTransactions(
        ids, 20, factories, hashers, encoder, ::temperature
    )


    @BeforeAll
    fun `initialize DB`() {
        transactions.forEach { transaction ->
            pw.persistEntity(transaction, transactionStorageAdapter).unwrapFailure()
        }
    }

    @Nested
    inner class Session {
        private val tid = pw.transactionStorageAdapter.id


        @Nested
        inner class Clusters {
            private val adapters = pw.providers

            @Test
            fun `created clusters`() {
                val plug = session as OrientSession
                val clusterNames = session.clustersPresent
                Logger.debug {
                    StringBuilder()
                        .appendLine()
                        .appendLine("Clusters present in ${plug.name}")
                        .appendByLine(clusterNames)
                        .toString()
                }
                assertThat(
                    clusterNames.mapToSet { set -> set.substringBeforeLast('_') }
                ).containsAll(
                    *adapters.map(SchemaProvider::id).map(String::toLowerCase).toTypedArray()
                )
            }

            @Test
            fun `cluster query`() {
                //Query from first cluster.
                val elements = session.queryToList(
                    "SELECT FROM CLUSTER:${tid}_1"
                )
                elements.forEach { res -> Logger.debug { res.json } }
                //Ensure there is a subset of the generated transactions
                //present.
                assertAll {
                    assertThat(elements.size).isLessThanOrEqualTo(transactions.size)
                    elements.map { element -> element.getHashProperty("hash") }.forEach { hash ->
                        assertThat(transactions.map(MutableTransaction::hash)).contains(hash)
                    }
                }

            }

        }

        @Test
        fun `transaction all properties present`() {
            val orient = (session as OrientSession)
            val present = orient.browseClass(tid).toList()
            assertThat(present.size).isEqualTo(transactions.size)
            val schemaProps =
                transactionStorageAdapter.properties.keys.toTypedArray()
            assertAll {
                present.forEach { element ->
                    assertThat(element.presentProperties).isNotNull().containsAll(*schemaProps)
                }
            }
            Logger.debug {
                StringBuilder("Properties in Transaction:")
                    .appendByLine(present[0].presentProperties)
                    .toString()
            }
            logActualToExpectedHashes(
                "Transactions' hashes from DB:",
                present.map { element -> element.getHashProperty("hash") },
                "Transactions' hashes from test:",
                transactions.map(MutableTransaction::hash)
            )
        }

        @Test
        fun `binary records`() {
            val elements = session.queryToList("SELECT FROM $tid")
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(*transactions.first().hash.bytes)
        }

        @Test
        fun `transaction with hash id`() {
            val randomTx = transactions[random.nextInt(transactions.size)]
            val elements = session.queryToList(
                UnspecificQuery(
                    "SELECT FROM $tid WHERE hash = :hash",
                    mapOf("hash" to randomTx.hash.bytes)
                )
            )
            assertThat(elements.isNotEmpty()).isTrue()
            val binary = elements[0].getHashProperty("hash")
            assertThat(binary.bytes).containsExactly(*randomTx.hash.bytes)
        }
    }

    @Nested
    inner class Handles {
        private val trafficChain: ChainHandle =
            ledger.registerNewChainHandleOf(trafficFlowDataStorageAdapter).unwrapFailure()

        val tempBlock = generateBlockWithChain(
            transactions, temperatureChain.chainId, factories, hashers, encoder
        )

        private val tempQueryManager = pw.chainManager(trafficChain.chainId)

        @BeforeAll
        fun `init blocks`() {
            //fill in witnesses.
            tempBlock.newExtraNonce()

            tempQueryManager.persistEntity(
                tempBlock, tempQueryManager.blockStorageAdapter
            ).unwrapFailure()
        }


        @Nested
        inner class Blocks {

            @Test
            fun `Test simple insertion`() {
                val block = generateBlockWithChain(
                    temperatureChain.chainId, factories, hashers, encoder
                )
                assertThat(block + transactions[0]).isTrue()
                assertThat(block.transactions[0]).isNotNull().isEqualTo(transactions[0])
            }

            @Test
            fun `Test transactions by index`() {
                val index = random.nextInt(transactions.size)
                tempQueryManager.getTransactionByIndex(tempBlock.blockHeader.hash, index)
                    .map { transaction ->
                        assertThat(transaction).isEqualTo(transactions[index])
                    }.failOnError()
            }

            //TODO: Depends on filling witnesses in block.
            @Test
            fun `Test fetch witness info`() {
                val randomTx = transactions[random.nextInt(transactions.size)]
                val publicKey = randomTx.publicKey
                tempQueryManager.getWitnessInfoBy(publicKey).map { info ->
                    assertThat(info.hash).isEqualTo(tempBlock.coinbase.coinbaseHeader.hash)
                    assertThat(info.index).isEqualTo(
                        tempBlock.coinbase.findWitness(randomTx.publicKey))
                    assertThat(info.max).isEqualTo(
                        tempBlock.coinbase.coinbaseHeader.blockheight)
                    Logger.debug(info::toString)
                }.failOnError()
            }

            /**
            @Test
            fun `Test fetch witness raw`() {
            val randomTx = transactions[random.nextInt(transactions.size)]
            val query = UnspecificQuery(
            """ SELECT coinbaseHeader.hash as hash,
            |coinbaseHeader.blockheight as blockheight,
            |witnesses:{index, publicKey} as witness
            |FROM ${tempQueryManager.coinbaseStorageAdapter.id}
            |UNWIND witness
            """.trimMargin(), mapOf("publicKey" to randomTx.publicKey.bytes)
            )
            val results = session.query(query)
            val items = results.toList()
            assertThat(items).isNotEmpty()
            }
             */

            @Test
            fun `Test transactions by time bound`() {
                val newT = generateXTransactions(
                    ids, 1, factories, hashers, encoder, ::temperature
                )[0]
                val current = newT.data.millis
                val diff = (current - transactions[0].data.millis).absoluteValue
                tempQueryManager.getTransactionByBound(current, diff).map { withBlockHash ->
                    val last = transactions.size - 1
                    assertThat(withBlockHash.txBlockHash).isEqualTo(tempBlock.blockHeader.hash)
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
                    ids, 1, factories, hashers, encoder, ::trafficFlow
                ).first()

                val block = generateBlockWithChain(
                    trafficChain.chainId, factories, hashers, encoder
                )
                assertThat(block).isNotNull()
                assertThat(block + testTraffic).isTrue()
                assertThat(block.transactions.first()).isEqualTo(testTraffic)
            }

        }
    }

    @Nested
    inner class Persistence {
        private val tempQueryManager: QueryManager =
            pw.chainManager(temperatureChain.chainId)

        @Test
        fun `loading transactions`() {
            tempQueryManager.getTransactionsByClass().map { list ->
                logActualToExpectedHashing(
                    "Transactions' hashes from DB:", list,
                    "Transactions' hashes from test:", transactions
                )
                assertThat(list.size).isEqualTo(transactions.size)
                assertThat(list).containsOnly(*transactions.toTypedArray())
            }.failOnError()
        }

        @Test
        fun `loading transactions by timestamp`() {
            tempQueryManager.getTransactionsOrderedByTimestamp().map { list ->
                logActualToExpectedHashing(
                    "Transactions' hashes from DB:", list,
                    "Transactions' hashes from test:", transactions
                )
                assertThat(list.size).isEqualTo(transactions.size)
                assertThat(list).containsOnly(*transactions.toTypedArray())
            }.failOnError()

        }

        @Test
        fun `loading transactions by Public Key`() {
            val key = ids[0].publicKey
            val expected = transactions.filter { transaction -> transaction.publicKey == key }
            tempQueryManager.getTransactionsFromAgent(key).map { list ->
                logActualToExpectedHashing(
                    "Transactions' hashes from DB from ${key.truncatedBase64Encoded()}:", list,
                    "Transactions' hashes from test from ${key.truncatedBase64Encoded()}:", expected
                )
                assertThat(list.size).isEqualTo(expected.size)
                assertThat(list).containsOnly(*expected.toTypedArray())
            }.failOnError()
        }

        @Test
        fun `loading transaction by hash`() {
            val randomTx = transactions.elementAt(random.nextInt(transactions.size))
            tempQueryManager.getTransactionByHash(randomTx.hash).map { transaction ->
                assertThat(transaction).isNotNull().isEqualTo(randomTx)
            }.failOnError()
        }
    }

    @AfterAll
    fun `close database`() {
        ledger.close()
    }

}