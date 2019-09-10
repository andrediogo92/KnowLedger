package org.knowledger.ledger.test

import assertk.fail
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.StorageUnawareChainId
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.GeoCoords
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.peekFailure
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.core.storage.results.QueryFailure
import org.knowledger.ledger.core.test.randomByteArray
import org.knowledger.ledger.core.test.randomDouble
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.serial.baseModule
import org.knowledger.ledger.serial.withDataFormulas
import org.knowledger.ledger.serial.withLedger
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.tinylog.kotlin.Logger
import java.math.BigDecimal

internal val testHasher: Hashers = DEFAULT_HASHER


internal val serialModule: SerialModule by lazy {
    baseModule.withLedger {
        TemperatureData::class with TemperatureData.serializer()
        TrafficFlowData::class with TrafficFlowData.serializer()
    }.withDataFormulas {}
}

internal val cbor: Cbor by lazy {
    Cbor(
        UpdateMode.OVERWRITE, true,
        serialModule
    )
}

@UnstableDefault
internal val json: Json = Json(
    configuration = JsonConfiguration.Default.copy(prettyPrint = true),
    context = serialModule
)


internal fun generateChainId(
    hasher: Hasher = DEFAULT_HASHER
): ChainId =
    StorageAwareChainId(
        StorageUnawareChainId(
            hasher, cbor,
            Hash(randomByteArray(32)),
            Hash(randomByteArray(32))
        )
    )


internal fun generateBlock(
    id: Array<Identity>,
    ts: List<HashedTransaction>,
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
            hasher, cbor,
            Hash(randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(
            hasher, coinbase,
            ts.toTypedArray()
        ), cbor, hasher
    )
}

internal fun generateXTransactions(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher
): List<HashedTransaction> {
    val ts: MutableList<HashedTransaction> = mutableListOf()
    for (i in 0 until size) {
        val index = i % id.size
        ts.add(
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
                ), hasher, cbor
            )
        )
    }
    return ts
}

internal fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher
): List<HashedTransaction> {
    val ts: MutableList<HashedTransaction> = mutableListOf()
    for (i in 0 until size) {
        ts.add(
            HashedTransactionImpl(
                id.privateKey, id.publicKey,
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
                ), hasher, cbor
            )
        )
    }
    return ts
}

internal fun generateBlockWithChain(
    chainId: ChainId,
    id: Array<Identity>,
    ts: List<HashedTransaction>,
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
            chainId, hasher, cbor,
            Hash(randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(hasher, coinbase, ts.toTypedArray()),
        cbor, hasher
    )
}

internal fun generateBlockWithChain(
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
            chainId, hasher, cbor,
            Hash(randomByteArray(32)),
            blockParams
        ), MerkleTreeImpl(hasher, coinbase, emptyArray()),
        cbor, hasher
    )
}

internal fun generateCoinbase(
    id: Array<Identity>,
    ts: List<HashedTransaction>,
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): HashedCoinbase {
    val sets = listOf(
        HashedTransactionOutputImpl(
            id[0].publicKey, emptyHash,
            Payout(BigDecimal.ONE),
            ts[0].hash, emptyHash,
            hasher, cbor
        ),
        HashedTransactionOutputImpl(
            id[1].publicKey, emptyHash,
            Payout(BigDecimal.ONE),
            ts[1].hash, emptyHash,
            hasher, cbor
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
        formula = formula, hasher = hasher, cbor = cbor
    )
}

internal fun generateCoinbase(
    hasher: Hashers = testHasher,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): HashedCoinbase =
    HashedCoinbaseImpl(
        transactionOutputs = mutableSetOf(),
        payout = Payout(BigDecimal("3")),
        difficulty = Difficulty.INIT_DIFFICULTY,
        blockheight = 2, coinbaseParams = coinbaseParams,
        formula = formula, hasher = hasher, cbor = cbor
    )

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

fun <T> Outcome<T, LoadFailure>.failOnLoadError() {
    peekFailure {
        when (it) {
            is LoadFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
            else -> {
            }
        }
        fail(it.cause)
    }
}

fun <T> Outcome<T, DataFailure>.failOnDataError() {
    peekFailure {
        when (it) {
            is DataFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
            else -> {
            }
        }
        fail(it.cause)
    }
}

fun <T : Any> Outcome<T, LedgerFailure>.failOnLedgerError() {
    peekFailure {
        when (it) {
            is LedgerFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
            else -> {
            }
        }
        fail(it.cause)
    }
}

fun <T : Any> Outcome<T, QueryFailure>.failOnQueryError() {
    peekFailure {
        when (it) {
            is QueryFailure.UnknownFailure ->
                it.exception?.let { ex ->
                    throw ex
                }
            else -> {
            }
        }
        fail(it.cause)
    }
}