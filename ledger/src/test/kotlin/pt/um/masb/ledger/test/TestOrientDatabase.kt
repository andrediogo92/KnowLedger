package pt.um.masb.ledger.test

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.tinylog.kotlin.Logger
import pt.um.masb.common.data.Difficulty.Companion.MIN_DIFFICULTY
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.database.orient.OrientSession
import pt.um.masb.common.database.query.UnspecificQuery
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.common.results.unwrap
import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.adapters.BlockParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.ChainIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TrafficFlowData
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.data.adapters.MerkleTreeStorageAdapter
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.data.adapters.TemperatureDataStorageAdapter
import pt.um.masb.ledger.data.adapters.TrafficFlowDataStorageAdapter
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.adapters.ChainHandleStorageAdapter
import pt.um.masb.ledger.service.adapters.IdentityStorageAdapter
import pt.um.masb.ledger.service.adapters.LedgerConfigStorageAdapter
import pt.um.masb.ledger.service.handles.ChainHandle
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.service.transactions.getTransactionByHash
import pt.um.masb.ledger.service.transactions.getTransactionsByClass
import pt.um.masb.ledger.service.transactions.getTransactionsFromAgent
import pt.um.masb.ledger.service.transactions.getTransactionsOrderedByTimestamp
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.adapters.BlockHeaderStorageAdapter
import pt.um.masb.ledger.storage.adapters.BlockStorageAdapter
import pt.um.masb.ledger.storage.adapters.CoinbaseStorageAdapter
import pt.um.masb.ledger.storage.adapters.TransactionOutputStorageAdapter
import pt.um.masb.ledger.storage.adapters.TransactionStorageAdapter
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

                val block = Block(
                    temperatureChain.id,
                    emptyHash,
                    MIN_DIFFICULTY,
                    1,
                    BlockParams()
                )
                assertThat(block.addTransaction(transactions[0]))
                    .isTrue()
                assertThat(block.data[0])
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

                val block = Block(
                    trafficChain.id,
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