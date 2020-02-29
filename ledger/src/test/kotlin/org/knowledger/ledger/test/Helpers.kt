package org.knowledger.ledger.test

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.collections.sortedListOf
import org.knowledger.collections.toSizedArray
import org.knowledger.collections.toSortedList
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.*
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.DataGenerator
import org.knowledger.testing.ledger.testHasher
import java.math.BigDecimal


val testSerialModule: SerialModule by lazy {
    SerializersModule {
        polymorphic(LedgerData::class) {
            TemperatureData::class with TemperatureData.serializer()
            TrafficFlowData::class with TrafficFlowData.serializer()
        }
    }
}

val testEncoder: BinaryFormat by lazy {
    Cbor(
        UpdateMode.OVERWRITE, true,
        testSerialModule
    )
}


@UnstableDefault
val testJson: Json = Json(
    configuration = JsonConfiguration.Default.copy(prettyPrint = true),
    context = testSerialModule
)


fun temperature(): LedgerData =
    TemperatureData(
        BigDecimal(
            random.randomDouble() * 100
        ), TemperatureUnit.Celsius
    )

fun trafficFlow(): LedgerData =
    TrafficFlowData(
        "FRC" + random.randomInt(6),
        random.randomInt(125), random.randomInt(125),
        random.randomInt(3000), random.randomInt(3000),
        random.randomDouble() * 34,
        random.randomDouble() * 12
    )


fun generateChainId(
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder
): ChainId =
    StorageAwareChainId(
        ChainIdImpl(
            hasher, encoder,
            Hash(random.randomByteArray(32)),
            Hash(random.randomByteArray(32))
        )
    )

fun transactionGenerator(
    id: Array<Identity>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): Sequence<Transaction> {
    return generateSequence {
        val index = random.randomInt(id.size)
        HashedTransactionImpl(
            id[index].privateKey,
            id[index].publicKey,
            PhysicalData(
                GeoCoords(
                    BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO
                ),
                generator()
            ), hasher, encoder
        )
    }
}

fun generateXTransactions(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): List<Transaction> =
    transactionGenerator(id, hasher, encoder, generator)
        .take(size)
        .toList()

fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): List<Transaction> =
    generateXTransactions(arrayOf(id), size, hasher, encoder, generator)

fun generateXTransactionsArray(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): Array<Transaction> =
    transactionGenerator(
        id, hasher, encoder, generator
    ).toSizedArray(size)

fun generateXTransactionsArray(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): Array<Transaction> =
    generateXTransactionsArray(
        arrayOf(id), size, hasher, encoder, generator
    )

fun generateBlock(
    id: Array<Identity>,
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher, encoder,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toSortedList(), coinbase,
        HashedBlockHeaderImpl(
            generateChainId(hasher),
            hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(
            hasher, coinbase, ts
        ), encoder, hasher
    )
}

fun generateBlockWithChain(
    chainId: ChainId,
    id: Array<Identity>,
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        id, ts, hasher, encoder,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toSortedList(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(hasher, coinbase, ts),
        encoder, hasher
    )
}

fun generateBlockWithChain(
    chainId: ChainId,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        hasher = hasher, encoder = encoder,
        formula = formula, coinbaseParams = coinbaseParams
    )
    return BlockImpl(
        sortedListOf(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ), MerkleTreeImpl(hasher, coinbase, emptyArray()),
        encoder, hasher
    )
}

fun generateCoinbase(
    id: Array<Identity>,
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase {
    val sets: MutableSet<TransactionOutput> = mutableSetOf(
        HashedTransactionOutputImpl(
            id[0].publicKey, Hash.emptyHash,
            Payout(BigDecimal.ONE),
            ts[0].hash, Hash.emptyHash,
            hasher, encoder
        ),
        HashedTransactionOutputImpl(
            id[1].publicKey, Hash.emptyHash,
            Payout(BigDecimal.ONE),
            ts[1].hash, Hash.emptyHash,
            hasher, encoder
        )
    )
    val first = sets.first()
    //First transaction output has
    //transaction 0.
    //Second is transaction 2
    //referencing transaction 0.
    //Third is transaction 4
    //referencing transaction 0.
    first.addToPayout(
        Payout(BigDecimal.ONE),
        ts[2].hash, ts[0].hash
    )
    first.addToPayout(
        Payout(BigDecimal.ONE),
        ts[4].hash, ts[0].hash
    )
    return generateCoinbase(
        transactionOutputs = sets,
        hasher = hasher,
        encoder = encoder,
        formula = formula,
        coinbaseParams = coinbaseParams
    )
}

fun generateCoinbase(
    transactionOutputs: MutableSet<TransactionOutput> = mutableSetOf(),
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase =
    HashedCoinbaseImpl(
        CoinbaseImpl(
            _transactionOutputs = transactionOutputs,
            _payout = Payout(BigDecimal("3")),
            _difficulty = Difficulty.INIT_DIFFICULTY,
            _blockheight = 2, coinbaseParams = coinbaseParams,
            formula = formula
        ), hasher = hasher, encoder = encoder
    )


fun Sequence<Transaction>.asTransactions(): List<Transaction> =
    asIterable().asTransactions()

internal fun Iterable<Transaction>.asTransactions(): List<Transaction> =
    map { (it as StorageAwareTransaction).transaction }.toList()