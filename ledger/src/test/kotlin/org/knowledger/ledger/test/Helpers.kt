package org.knowledger.ledger.test

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.GeoCoords
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageResult
import org.knowledger.ledger.core.database.StorageResults
import org.knowledger.ledger.core.database.query.GenericQuery
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Failure
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.peekFailure
import org.knowledger.ledger.core.results.unwrap
import org.knowledger.ledger.core.test.randomByteArray
import org.knowledger.ledger.core.test.randomDouble
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.serial.withDataFormulas
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

val testHasher: Hashers = DEFAULT_HASHER


val serialModule: SerialModule by lazy {
    baseModule.withLedger {
        TemperatureData::class with TemperatureData.serializer()
        TrafficFlowData::class with TrafficFlowData.serializer()
    }.withDataFormulas {}
}

val encoder: BinaryFormat by lazy {
    Cbor(
        UpdateMode.OVERWRITE, true,
        serialModule
    )
}


@UnstableDefault
val json: Json = Json(
    configuration = JsonConfiguration.Default.copy(prettyPrint = true),
    context = serialModule
)


fun generateChainId(
    hasher: Hasher = DEFAULT_HASHER
): ChainId =
    StorageAwareChainId(
        ChainIdImpl(
            hasher, encoder,
            Hash(randomByteArray(32)),
            Hash(randomByteArray(32))
        )
    )


fun generateBlock(
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toSortedSet(), coinbase,
        HashedBlockHeaderImpl(
            generateChainId(hasher),
            hasher, encoder,
            Hash(randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(
            hasher, coinbase,
            ts.toTypedArray()
        ), encoder, hasher
    )
}

fun transactionGenerator(
    id: Array<Identity>,
    hasher: Hashers = testHasher
): Sequence<Transaction> {
    var i = 0
    return generateSequence {
        val index = i % id.size
        HashedTransactionImpl(
            id[index].privateKey,
            id[index].publicKey,
            PhysicalData(
                GeoCoords(
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO
                ),
                TemperatureData(
                    BigDecimal(
                        randomDouble() * 100
                    ), TemperatureUnit.Celsius
                )
            ), hasher, encoder
        ).also {
            i++
        }
    }
}

fun generateXTransactions(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher
): List<Transaction> =
    transactionGenerator(id, hasher)
        .take(size)
        .toList()

fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher
): List<Transaction> =
    transactionGenerator(
        arrayOf(id), hasher
    ).take(size).toList()


fun generateBlockWithChain(
    chainId: ChainId,
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toSortedSet(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(hasher, coinbase, ts.toTypedArray()),
        encoder, hasher
    )
}

fun generateBlockWithChain(
    chainId: ChainId,
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        hasher, formula, coinbaseParams
    )
    return BlockImpl(
        sortedSetOf(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(randomByteArray(32)),
            blockParams
        ), MerkleTreeImpl(hasher, coinbase, emptyArray()),
        encoder, hasher
    )
}

fun generateCoinbase(
    id: Array<Identity>,
    ts: List<Transaction>,
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase {
    val sets = listOf(
        HashedTransactionOutputImpl(
            id[0].publicKey, emptyHash,
            Payout(BigDecimal.ONE),
            ts[0].hash, emptyHash,
            hasher, encoder
        ),
        HashedTransactionOutputImpl(
            id[1].publicKey, emptyHash,
            Payout(BigDecimal.ONE),
            ts[1].hash, emptyHash,
            hasher, encoder
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
        ts[2].hash, ts[0].hash
    )
    sets[0].addToPayout(
        Payout(BigDecimal.ONE),
        ts[4].hash, ts[0].hash
    )
    return HashedCoinbaseImpl(
        transactionOutputs = sets.toMutableSet(),
        payout = Payout(BigDecimal("3")),
        difficulty = Difficulty.INIT_DIFFICULTY,
        blockheight = 2, coinbaseParams = coinbaseParams,
        formula = formula, hasher = hasher, encoder = encoder
    )
}

fun generateCoinbase(
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase =
    HashedCoinbaseImpl(
        transactionOutputs = mutableSetOf(),
        payout = Payout(BigDecimal("3")),
        difficulty = Difficulty.INIT_DIFFICULTY,
        blockheight = 2, coinbaseParams = coinbaseParams,
        formula = formula, hasher = hasher, encoder = encoder
    )

private fun StorageResults.toList(): List<StorageElement> =
    asSequence().map(StorageResult::element).toList()

internal fun Iterable<Transaction>.asTransactions(): List<Transaction> =
    this.map { (it as StorageAwareTransaction).transaction }.toList()

internal fun Sequence<Transaction>.asTransactions(): List<Transaction> =
    asIterable().asTransactions()


internal fun ManagedSession.queryToList(
    query: String
): List<StorageElement> =
    query(query).toList()

internal fun ManagedSession.queryToList(
    query: GenericQuery
): List<StorageElement> =
    query(query).toList()


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

internal fun StringBuilder.appendByLine(toPrint: Collection<String>): StringBuilder =
    apply {
        toPrint.forEach { thing ->
            append(System.lineSeparator())
            append('\t')
            append(thing)
            append(',')
        }
    }

fun <T> Outcome<T, Failure>.failOnError() {
    peekFailure {
        it.unwrap()
    }
}