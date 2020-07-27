package org.knowledger.testing.storage

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.UpdateMode
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.classDigest
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.storage.*
import org.knowledger.ledger.storage.mutations.indexed
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.testing.core.DataGenerator
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.knowledger.testing.core.randomData
import org.knowledger.testing.ledger.RandomData
import java.math.BigDecimal

@OptIn(UnstableDefault::class)
fun defaultJson(serialModule: SerialModule): StringFormat =
    Json(JsonConfiguration.Default.copy(prettyPrint = true), serialModule)

fun defaultEncoder(serialModule: SerialModule): BinaryFormat =
    Cbor(UpdateMode.OVERWRITE, true, serialModule)

val defaultModule: SerialModule by lazy {
    SerializersModule {
        polymorphic(LedgerData::class) {
            RandomData::class with RandomData.serializer()
        }
    }
}

val defaultJson: StringFormat by lazy {
    defaultJson(defaultModule)
}

val defaultCbor: BinaryFormat by lazy {
    defaultEncoder(defaultModule)
}

val defaultFactories: Factories by lazy { suFactories }


fun generateBlockParams(
    blockMemorySize: Int = 2097152, blockLength: Int = 512,
    factories: Factories = defaultFactories
): BlockParams =
    factories.blockParamsFactory.create(
        blockMemorySize = blockMemorySize,
        blockLength = blockLength
    )

fun generateCoinbaseParams(
    hashSize: Int = defaultHasher.hashSize,
    timeIncentive: Long = 5, valueIncentive: Long = 2,
    baseIncentive: Long = 3, dividingThreshold: Long = 100000,
    formula: Hash = classDigest<DefaultDiff>(Hashers.SHA3512Hasher),
    factories: Factories = defaultFactories
): CoinbaseParams =
    factories.coinbaseParamsFactory.create(
        hashSize = hashSize, timeIncentive = timeIncentive,
        valueIncentive = valueIncentive, baseIncentive = baseIncentive,
        dividingThreshold = dividingThreshold, formula = formula
    )

fun generateLedgerParams(
    hasher: Hash = defaultHasher.id,
    recalculationTime: Long = 1228800000,
    recalculationTrigger: Int = 2048,
    factories: Factories = defaultFactories
): LedgerParams =
    factories.ledgerParamsFactory.create(
        hasher = hasher, recalculationTime = recalculationTime,
        recalculationTrigger = recalculationTrigger
    )

fun generateChainId(
    ledgerHash: Hash, adapter: SchemaProvider,
    factories: Factories = defaultFactories,
    blockParams: BlockParams = generateBlockParams(),
    coinbaseParams: CoinbaseParams = generateCoinbaseParams(),
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): ChainId = factories.chainIdFactory.create(
    adapter.id.base64DecodedToHash(), ledgerHash,
    hasher, encoder,
    blockParams, coinbaseParams
)

fun immutableTransactionGenerator(
    id: Array<Identity>,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData
): Sequence<ImmutableTransaction> =
    transactionGenerator(
        id, factories, hasher, encoder, generator
    ).map { it.immutableCopy() }

internal fun transactionGenerator(
    id: Array<Identity>,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData
): Sequence<MutableTransaction> = generateSequence {
    val index = random.randomInt(id.size)
    factories.transactionFactory.create(
        id[index].privateKey, id[index].publicKey,
        PhysicalData(
            GeoCoords(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
            generator()
        ), hasher, encoder
    )
}

fun generateImmutableXTransactions(
    id: Array<Identity>, size: Int,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData
): MutableSortedList<ImmutableTransaction> =
    immutableTransactionGenerator(
        id, factories, hasher, encoder, generator
    ).take(size).toMutableSortedList()


fun generateXTransactions(
    id: Array<Identity>, size: Int,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData
): MutableSortedList<MutableTransaction> =
    transactionGenerator(
        id, factories, hasher, encoder, generator
    ).take(size).toMutableSortedList()

fun generateBlock(
    ts: MutableSortedList<MutableTransaction>,
    ledgerHash: Hash, adapter: SchemaProvider,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock = generateBlockWithChain(
    ts, generateChainId(ledgerHash, adapter),
    factories, hasher, encoder
)

fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>,
    chainId: ChainId, factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock = generateBlockWithChain(
    ts, generateCoinbase(
        chainId.coinbaseParams, factories, hasher, encoder
    ), chainId.hash, chainId.blockParams, factories, hasher, encoder
)


fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>,
    chainHash: Hash, coinbaseParams: CoinbaseParams,
    blockParams: BlockParams,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock = generateBlockWithChain(
    ts, generateCoinbase(
        coinbaseParams, factories, hasher, encoder
    ), chainHash, blockParams, factories, hasher, encoder
)

fun generateBlockWithChain(
    chainId: ChainId,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock = generateBlockWithChain(
    mutableSortedListOf(), generateCoinbase(
        chainId.coinbaseParams, factories, hasher, encoder
    ), chainId.hash, chainId.blockParams, factories, hasher, encoder
)

fun generateBlockWithChain(
    chainHash: Hash, coinbaseParams: CoinbaseParams,
    blockParams: BlockParams,
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock = generateBlockWithChain(
    mutableSortedListOf(), generateCoinbase(
        coinbaseParams, factories, hasher, encoder
    ), chainHash, blockParams, factories, hasher, encoder
)

fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>,
    coinbase: MutableCoinbase, chainHash: Hash,
    blockParams: BlockParams = generateBlockParams(),
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableBlock {
    val merkle = factories.merkleTreeFactory.create(
        hasher, coinbase.coinbaseHeader, ts.toTypedArray()
    )
    return factories.blockFactory.create(
        ts.indexed(), coinbase,
        factories.blockHeaderFactory.create(
            chainHash, Hash(random.randomByteArray(32)),
            blockParams, hasher, encoder, merkle.hash
        ), merkle
    )
}

fun generateCoinbase(
    coinbaseParams: CoinbaseParams = generateCoinbaseParams(),
    factories: Factories = defaultFactories,
    hasher: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor
): MutableCoinbase {
    val merkle = factories.merkleTreeFactory.create(
        hasher, arrayOf()
    )
    return factories.coinbaseFactory.create(
        factories.coinbaseHeaderFactory.create(
            merkle.hash, 1, coinbaseParams, hasher, encoder
        ), merkle, mutableSortedListOf()
    )
}