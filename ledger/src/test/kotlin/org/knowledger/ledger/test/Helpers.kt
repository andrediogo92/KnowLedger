package org.knowledger.ledger.test

import assertk.fail
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.data.DataFormula
import org.knowledger.common.data.DefaultDiff
import org.knowledger.common.data.Difficulty
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.Payout
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hash.Companion.emptyHash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.decodeUTF8ToString
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.peekFailure
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.common.storage.results.QueryFailure
import org.knowledger.common.test.randomByteArray
import org.knowledger.common.test.randomDouble
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.data.MerkleTree
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.TUnit
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.json.BigDecimalJsonAdapter
import org.knowledger.ledger.json.BigIntegerJsonAdapter
import org.knowledger.ledger.json.HashJsonAdapter
import org.knowledger.ledger.json.InstantJsonAdapter
import org.knowledger.ledger.json.PublicKeyJsonAdapter
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

internal val moshi by lazy {
    Moshi
        .Builder()
        .add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(
            PolymorphicJsonAdapterFactory
                .of(LedgerData::class.java, "type")
                .withSubtype(TemperatureData::class.java, "Temperature")
                .withSubtype(TrafficFlowData::class.java, "TrafficFlowData")
        )
        .build()
}

internal fun generateChainId(
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
): ChainId =
    ChainId(
        randomByteArray(32).decodeUTF8ToString(),
        Hash(randomByteArray(32)), hasher
    )


internal fun generateBlock(
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher, formula, coinbaseParams
    )
    return Block(
        mutableListOf(),
        coinbase,
        BlockHeader(
            generateChainId(hasher),
            hasher,
            Hash(randomByteArray(32)),
            Difficulty.INIT_DIFFICULTY, 2,
            blockParams
        ),
        MerkleTree(hasher, coinbase, ts)
    )
}

internal fun generateXTransactions(
    id: Array<Identity>,
    size: Int,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
                generateChainId(hasher),
                id[index].privateKey,
                id[index].publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                ),
                hasher
            )
        )
    }
    return ts
}

internal fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
                generateChainId(hasher),
                id.privateKey,
                id.publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                ),
                hasher
            )
        )
    }
    return ts
}

internal fun generateBlockWithChain(
    chainId: ChainId,
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher, formula, coinbaseParams
    )
    return Block(
        mutableListOf(),
        coinbase,
        BlockHeader(
            chainId,
            hasher,
            Hash(randomByteArray(32)),
            Difficulty.INIT_DIFFICULTY, 2,
            blockParams
        ),
        MerkleTree(hasher, coinbase, ts)
    )
}

internal fun generateXTransactionsWithChain(
    chainId: ChainId,
    id: Array<Identity>,
    size: Int,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
                chainId,
                id[index].privateKey,
                id[index].publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                ),
                hasher
            )
        )
    }
    return ts
}

internal fun generateXTransactionsWithChain(
    chainId: ChainId,
    id: Identity,
    size: Int,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
                chainId,
                id.privateKey,
                id.publicKey,
                PhysicalData(
                    TemperatureData(
                        BigDecimal(
                            randomDouble() * 100
                        ),
                        TUnit.CELSIUS
                    )
                ),
                hasher
            )
        )
    }
    return ts
}


internal fun generateCoinbase(
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hasher = LedgerConfiguration.DEFAULT_CRYPTER,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase {
    val sets = listOf(
        TransactionOutput(
            id[0].publicKey,
            emptyHash,
            Payout(BigDecimal.ONE),
            ts[0].hashId,
            emptyHash,
            hasher
        ),
        TransactionOutput(
            id[1].publicKey,
            emptyHash,
            Payout(BigDecimal.ONE),
            ts[1].hashId,
            emptyHash,
            hasher
        )
    )
    //First transaction output has
    //transaction 0.
    //Second is transaction 2
    //referencing transaction 0.
    //Third is transaction 4
    //referencing transaction 0.
    sets[0].addToPayout(
        Payout(BigDecimal.ONE),
        ts[2].hashId,
        ts[0].hashId
    )
    sets[0].addToPayout(
        Payout(BigDecimal.ONE),
        ts[4].hashId,
        ts[0].hashId
    )
    return Coinbase(
        sets.toSet() as MutableSet<TransactionOutput>,
        Payout(BigDecimal("3")),
        emptyHash,
        hasher,
        formula,
        coinbaseParams
    )
}

internal fun logActualToExpectedLists(
    explanationActual: String,
    actualList: List<Any>,
    explanationExpected: String,
    expectedList: List<Any>
) {
    Logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it.toString() }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,
                |
            """.trimMargin()
        ) { it.toString() }}
        """.trimMargin()
    }
}

internal fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder {
    for (thing in toPrint) {
        append(System.lineSeparator())
        append('\t')
        append(thing)
        append(',')
    }
    return this
}

fun <T> Outcome<T, LoadFailure>.failOnLoadError() {
    this.peekFailure {
        when (it) {
            is LoadFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
        }
        fail(it.cause)
    }
}

fun <T> Outcome<T, DataFailure>.failOnDataError() {
    this.peekFailure {
        when (it) {
            is DataFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
        }
        fail(it.cause)
    }
}

fun <T : Any> Outcome<T, LedgerFailure>.failOnLedgerError() {
    this.peekFailure {
        when (it) {
            is LedgerFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
        }
        fail(it.cause)
    }
}

fun <T : Any> Outcome<T, QueryFailure>.failOnQueryError() {
    this.peekFailure {
        when (it) {
            is QueryFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
        }
        fail(it.cause)
    }
}