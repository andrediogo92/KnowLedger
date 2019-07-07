package org.knowledger.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.common.data.Difficulty.Companion.MIN_DIFFICULTY
import org.knowledger.common.database.DatabaseMode
import org.knowledger.common.database.DatabaseType
import org.knowledger.common.database.orient.OrientDatabase
import org.knowledger.common.database.orient.OrientDatabaseInfo
import org.knowledger.common.database.orient.OrientSession
import org.knowledger.common.database.query.UnspecificQuery
import org.knowledger.common.hash.Hash.Companion.emptyHash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.results.mapSuccess
import org.knowledger.common.results.unwrap
import org.knowledger.common.storage.adapters.SchemaProvider
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.adapters.BlockParamsStorageAdapter
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.data.adapters.DummyDataStorageAdapter
import org.knowledger.ledger.data.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.data.adapters.TemperatureDataStorageAdapter
import org.knowledger.ledger.data.adapters.TrafficFlowDataStorageAdapter
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.adapters.ChainHandleStorageAdapter
import org.knowledger.ledger.service.adapters.IdentityStorageAdapter
import org.knowledger.ledger.service.adapters.LedgerConfigStorageAdapter
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.transactions.getTransactionByHash
import org.knowledger.ledger.service.transactions.getTransactionsByClass
import org.knowledger.ledger.service.transactions.getTransactionsFromAgent
import org.knowledger.ledger.service.transactions.getTransactionsOrderedByTimestamp
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.StorageUnawareBlock
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

class TestOrientDatabase {
    val id = Identity("test")

    val db = OrientDatabase(
        OrientDatabaseInfo(
            databaseMode = DatabaseMode.MEMORY,
            databaseType = DatabaseType.MEMORY,
            path = "test"
        )
    )
    val session = db.newManagedSession("test")
    val ledger = LedgerHandle
        .Builder()
        .withLedgerIdentity("test")
        .unwrap()
        .withCustomDB(db, session)
        .build()
        .unwrap()

    val hash = ledger.ledgerConfig.ledgerId.hashId

    val temperatureChain: ChainHandle = ledger.registerNewChainHandleOf(
        TemperatureDataStorageAdapter
    ).unwrap()

    val chainId = temperatureChain.id
    val chainHash = temperatureChain.id.hashId
    internal val pw = LedgerHandle.getContainer(hash)!!.persistenceWrapper
    val transactions = generateXTransactionsWithChain(chainId, id, 20)


    @BeforeAll
    fun `initialize DB`() {
        transactions.forEach {
            pw.persistEntity(it, TransactionStorageAdapter)
        }
    }

    @Nested
    inner class Session {
        val adapters: List<SchemaProvider<out Any>> = listOf(
            //Configuration Adapters
            BlockParamsStorageAdapter,
            ChainIdStorageAdapter,
            CoinbaseStorageAdapter,
            LedgerConfigStorageAdapter,
            LedgerIdStorageAdapter,
            LedgerParamsStorageAdapter,
            //ServiceAdapters
            ChainHandleStorageAdapter,
            IdentityStorageAdapter,
            //StorageAdapters
            BlockHeaderStorageAdapter,
            BlockStorageAdapter,
            CoinbaseStorageAdapter,
            MerkleTreeStorageAdapter,
            PhysicalDataStorageAdapter,
            TransactionOutputStorageAdapter,
            TransactionStorageAdapter,
            //DataAdapters
            DummyDataStorageAdapter
        )
        val tid = TransactionStorageAdapter.id


        @Nested
        inner class Clusters {
            @Test
            fun `created clusters`() {
                val plug = session as OrientSession
                val clusterNames = session.clustersPresent
                Logger.info {
                    StringBuilder()
                        .append("Clusters present in ${plug.name}")
                        .appendByLine(clusterNames)
                        .toString()
                }
                assertThat(
                    clusterNames.asSequence().map {
                        it.substringBeforeLast('_')
                    }.toSet()
                ).containsAll(
                    *adapters.map {
                        it.id.toLowerCase()
                    }.toTypedArray()
                )
            }

            @Test
            fun `cluster query`() {
                //Query from first cluster.
                session.query(
                    """
                    SELECT 
                    FROM CLUSTER:${tid}_1
                """.trimIndent()
                ).let { set ->
                    val l = set.asSequence().toList()
                    l.forEach { res ->
                        Logger.info {
                            res.element.print()
                        }
                    }
                    //Ensure there is a subset of the generated transactions
                    //present.
                    assertAll {
                        assertThat(l.size).isLessThanOrEqualTo(transactions.size)
                        l.map {
                            it.element.getHashProperty("hashId")
                        }.forEach { hash ->
                            assertThat(transactions.map {
                                it.hashId
                            }).contains(
                                hash
                            )
                        }
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
                TransactionStorageAdapter.properties.keys.toTypedArray()
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
            logActualToExpectedLists(
                "Transactions' hashes from DB:",
                present.map {
                    it.getHashProperty("hashId").print
                },
                "Transactions' hashes from test:",
                transactions.map { it.hashId.print }
            )
        }

        @Test
        fun `binary records`() {
            val t = session.query(
                """
                SELECT 
                FROM $tid
            """.trimIndent()
            )
            assertThat(t.hasNext()).isTrue()
            val binary = t.next().element.getHashProperty("hashId")
            assertThat(binary.bytes).containsExactly(
                *transactions[0].hashId.bytes
            )
        }

        @Test
        fun `transaction with hash id`() {
            val t = session.query(
                UnspecificQuery(
                    """
                    SELECT
                    FROM $tid
                    WHERE hashId = :hash
                """.trimIndent(),
                    mapOf(
                        "hash" to transactions[0].hashId.bytes
                    )
                )
            )
            assertThat(t.hasNext()).isTrue()
            val binary = t.next().element.getHashProperty("hashId")
            assertThat(binary.bytes).containsExactly(
                *transactions[0].hashId.bytes
            )

        }
    }

    @Nested
    inner class Handles {
        val hasher: Hasher = LedgerHandle.getHasher(hash)!!

        val trafficChain: ChainHandle = ledger.registerNewChainHandleOf(
            TrafficFlowDataStorageAdapter
        ).unwrap()

        @Nested
        inner class Blocks {

            @Test
            fun `Test simple insertion`() {

                val block = StorageUnawareBlock(
                    temperatureChain.id,
                    emptyHash,
                    MIN_DIFFICULTY,
                    1,
                    BlockParams()
                )
                assertThat(block + transactions[0])
                    .isTrue()
                assertThat(block.data.first())
                    .isNotNull()
                    .isEqualTo(transactions[0])
            }


            @Test
            fun `Test traffic insertion`() {
                val testTraffic = Transaction(
                    trafficChain.id,
                    id.privateKey,
                    id.publicKey,
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

                val block = StorageUnawareBlock(
                    trafficChain.id,
                    emptyHash,
                    MIN_DIFFICULTY,
                    1,
                    BlockParams()
                )
                assertThat(block).isNotNull()
                assertThat(block + testTraffic)
                    .isTrue()
                assertThat(block.data.first())
                    .isNotNull()
                    .isEqualTo(testTraffic)
                Logger.info {
                    moshi.adapter<Block>(Block::class.java)
                        .toJson(block)
                }
            }

        }
    }

    @Nested
    inner class Persistence {

        @Test
        fun `loading transactions`() {
            pw.getTransactionsByClass(
                chainHash,
                TemperatureDataStorageAdapter.id
            ).mapSuccess { seq ->
                seq.toList().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hashId.print },
                        "Transactions' hashes from test:",
                        transactions.map { it.hashId.print }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsOnly(
                        *transactions.toTypedArray()
                    )
                }
            }.failOnLoadError()
        }

        @Test
        fun `loading transactions by timestamp`() {
            pw.getTransactionsOrderedByTimestamp(
                chainHash
            ).mapSuccess { seq ->
                seq.toList().apply {
                    val reversed = transactions.asReversed()
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hashId.print },
                        "Transactions' hashes from test:",
                        reversed.map { it.hashId.print }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsExactly(
                        *transactions.asReversed().toTypedArray()
                    )

                }
            }.failOnLoadError()

        }

        @Test
        fun `loading transactions by Public Key`() {
            pw.getTransactionsFromAgent(
                chainHash, id.publicKey
            ).mapSuccess { seq ->
                seq.toList().apply {
                    logActualToExpectedLists(
                        "Transactions' hashes from DB:",
                        map { it.hashId.print },
                        "Transactions' hashes from test:",
                        transactions.map { it.hashId.print }
                    )
                    assertThat(size).isEqualTo(
                        transactions.size
                    )
                    assertThat(this).containsOnly(
                        *transactions.toTypedArray()
                    )
                }
            }.failOnLoadError()
        }

        @Test
        fun `loading transaction by hash`() {
            pw.getTransactionByHash(
                chainHash,
                transactions[2].hashId
            ).mapSuccess {
                assertThat(it)
                    .isNotNull()
                    .isEqualTo(transactions[2])
            }.failOnLoadError()
        }
    }

    @AfterAll
    fun `close database`() {
        ledger.close()
    }

}