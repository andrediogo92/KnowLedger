package org.knowledger.ledger.test

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedList
import org.knowledger.collections.toSizedArray
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.DefaultDiff
import org.knowledger.ledger.data.GeoCoords
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.data.TrafficFlowData
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.CoinbaseImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
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
): MutableSortedList<Transaction> =
    transactionGenerator(id, hasher, encoder, generator)
        .take(size)
        .toMutableSortedList()

fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    generator: DataGenerator = ::temperature
): MutableSortedList<Transaction> =
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
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block =
    generateBlock(
        ts.toMutableSortedList(), hasher,
        encoder, formula, coinbaseParams,
        blockParams
    )

fun generateBlockWithChain(
    chainId: ChainId,
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block =
    generateBlockWithChain(
        chainId, ts.toMutableSortedList(),
        hasher, encoder, formula,
        coinbaseParams, blockParams
    )

fun generateBlock(
    ts: MutableSortedList<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        ts.toTypedArray(), hasher, encoder,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toMutableSortedList(), coinbase,
        HashedBlockHeaderImpl(
            generateChainId(hasher),
            hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(
            hasher, coinbase, ts.toTypedArray()
        ), encoder, hasher
    )
}

fun generateBlockWithChain(
    chainId: ChainId,
    ts: MutableSortedList<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    blockParams: BlockParams = BlockParams()
): Block {
    val coinbase = generateCoinbase(
        ts.toTypedArray(), hasher, encoder,
        formula, coinbaseParams
    )
    return BlockImpl(
        ts.toMutableSortedList(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ),
        MerkleTreeImpl(hasher, coinbase, ts.toTypedArray()),
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
        mutableSortedListOf(), coinbase,
        HashedBlockHeaderImpl(
            chainId, hasher, encoder,
            Hash(random.randomByteArray(32)),
            blockParams
        ), MerkleTreeImpl(hasher, coinbase, emptyArray()),
        encoder, hasher
    )
}

fun generateCoinbase(
    ts: Array<Transaction>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff,
    coinbaseParams: CoinbaseParams = CoinbaseParams()
): Coinbase =
    generateCoinbase(
        coinbaseParams = coinbaseParams,
        hasher = hasher,
        encoder = encoder,
        formula = formula
    ).apply {
        addToWitness(
            newIndex = 0,
            newTransaction = ts[0]
        )
        findAndAdd(1, ts[1])

        //First transaction output has
        //transaction 0.
        //Second is transaction 2
        //referencing transaction 0.
        //Third is transaction 4
        //referencing transaction 0.
        findAndAdd(0, ts[0], 2, ts[2])
        findAndAdd(0, ts[0], 4, ts[4])
    }

fun generateCoinbase(
    coinbaseParams: CoinbaseParams = CoinbaseParams(),
    witnesses: MutableSortedList<Witness> = mutableSortedListOf(),
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder,
    formula: DataFormula = DefaultDiff
): Coinbase =
    HashedCoinbaseImpl(
        CoinbaseImpl(
            coinbaseParams = coinbaseParams,
            _witnesses = witnesses,
            formula = formula
        ), hasher = hasher, encoder = encoder
    )

fun Coinbase.findAndAdd(newIndex: Int, newTransaction: Transaction) {
    val index = findWitness(newTransaction)
    if (index >= 0) {
        addToWitness(
            witness = witnesses[index],
            newIndex = newIndex,
            newTransaction = newTransaction
        )
    } else {
        addToWitness(
            newIndex = newIndex,
            newTransaction = newTransaction
        )
    }
}

fun Coinbase.findAndAdd(
    latestKnownIndex: Int, latestKnown: Transaction,
    newIndex: Int, newTransaction: Transaction
) {
    val index = findWitness(newTransaction)
    if (index >= 0) {
        addToWitness(
            witness = witnesses[index],
            newIndex = newIndex,
            newTransaction = newTransaction,
            latestKnownIndex = latestKnownIndex,
            latestKnown = latestKnown
        )
    } else {
        addToWitness(
            newIndex = newIndex,
            newTransaction = newTransaction,
            latestKnownIndex = latestKnownIndex,
            latestKnown = latestKnown
        )
    }
}

fun Sequence<Transaction>.asTransactions(): List<Transaction> =
    asIterable().asTransactions()

internal fun Iterable<Transaction>.asTransactions(): List<Transaction> =
    map { (it as StorageAwareTransaction).transaction }.toList()