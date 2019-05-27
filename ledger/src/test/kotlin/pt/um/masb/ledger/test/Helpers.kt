package pt.um.masb.ledger.test

import assertk.fail
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import mu.KLogger
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.DataFormula
import pt.um.masb.common.data.DefaultDiff
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Payout
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.common.storage.results.QueryFailure
import pt.um.masb.common.test.randomByteArray
import pt.um.masb.common.test.randomDouble
import pt.um.masb.ledger.config.BlockParams
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.data.TUnit
import pt.um.masb.ledger.data.TemperatureData
import pt.um.masb.ledger.data.TrafficFlowData
import pt.um.masb.ledger.json.BigDecimalJsonAdapter
import pt.um.masb.ledger.json.BigIntegerJsonAdapter
import pt.um.masb.ledger.json.HashJsonAdapter
import pt.um.masb.ledger.json.InstantJsonAdapter
import pt.um.masb.ledger.json.PublicKeyJsonAdapter
import pt.um.masb.ledger.service.Identity
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.TransactionOutput
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
                .of(BlockChainData::class.java, "type")
                .withSubtype(TemperatureData::class.java, "Temperature")
                .withSubtype(TrafficFlowData::class.java, "TrafficFlowData")
        )
        .build()
}

internal fun generateBlock(
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hasher = AvailableHashAlgorithms.SHA256Hasher,
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
            Hash(randomByteArray(32)),
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
    hasher: Hasher = AvailableHashAlgorithms.SHA256Hasher
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
            Transaction(
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
    hasher: Hasher = AvailableHashAlgorithms.SHA256Hasher
): List<Transaction> {
    val ts: MutableList<Transaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            Transaction(
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
    hasher: Hasher = AvailableHashAlgorithms.SHA256Hasher,
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
    expectedList: List<Any>,
    logger: KLogger
) {
    logger.info {
        """
            |
            |$explanationActual
            |${actualList.joinToString(
            """,

            """.trimIndent()
        ) { it.toString() }}
            |
            |$explanationExpected
            |${expectedList.joinToString(
            """,

            """.trimIndent()
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

fun <T : Any> Outcome<T, LoadFailure>.failOnLoadError() {
    this.mapError<LoadFailure> {
        when (this.failure) {
            is LoadFailure.UnknownFailure ->
                (this.failure as LoadFailure.UnknownFailure).exception?.let {
                    throw it
                }
        }
        fail(this.failure.cause)
    }
}

fun <T : Any> Outcome<T, DataFailure>.failOnDataError() {
    this.mapError<DataFailure> {
        when (this.failure) {
            is DataFailure.UnknownFailure ->
                (this.failure as DataFailure.UnknownFailure).exception?.let {
                    throw it
                }
        }
        fail(this.failure.cause)
    }
}

fun <T : Any> Outcome<T, LedgerFailure>.failOnLedgerError() {
    this.mapError<LedgerFailure> {
        when (this.failure) {
            is LedgerFailure.UnknownFailure ->
                (this.failure as LedgerFailure.UnknownFailure).exception?.let {
                    throw it
                }
        }
        fail(this.failure.cause)
    }
}

fun <T : Any> Outcome<T, QueryFailure>.failOnQueryError() {
    this.mapError<QueryFailure> {
        when (this.failure) {
            is QueryFailure.UnknownFailure ->
                (this.failure as QueryFailure.UnknownFailure).exception?.let {
                    throw it
                }
        }
        fail(this.failure.cause)
    }
}