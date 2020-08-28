@file:OptIn(ExperimentalSerializationApi::class)

package org.knowledger.testing.storage

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.StringFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
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
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.DefaultDiff
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.GeoCoords
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.immutableCopy
import org.knowledger.ledger.storage.mutations.indexed
import org.knowledger.ledger.storage.suFactories
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.testing.core.DataGenerator
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.knowledger.testing.core.randomData
import org.knowledger.testing.ledger.RandomData
import java.math.BigDecimal

fun defaultJson(serializersModule: SerializersModule): StringFormat =
    Json {
        prettyPrint = true
        this.serializersModule = serializersModule
    }

@OptIn(ExperimentalSerializationApi::class)
fun defaultEncoder(serializersModule: SerializersModule): BinaryFormat =
    Cbor {
        encodeDefaults = true
        this.serializersModule = serializersModule
    }

val defaultModule: SerializersModule by lazy {
    SerializersModule {
        polymorphic(LedgerData::class, RandomData::class, RandomData.serializer())
        polymorphic(DataFormula::class, DefaultDiff::class, DefaultDiff.serializer())
    }
}

val defaultJson: StringFormat by lazy { defaultJson(defaultModule) }

val defaultCbor: BinaryFormat by lazy { defaultEncoder(defaultModule) }

val defaultFactories: Factories by lazy { suFactories }


fun generateBlockParams(
    blockMemorySize: Int = 2097152, blockLength: Int = 512, factories: Factories = defaultFactories,
): BlockParams = factories.blockParamsFactory.create(blockMemorySize, blockLength)

fun generateCoinbaseParams(
    hashSize: Int = defaultHasher.hashSize, timeIncentive: Long = 5, valueIncentive: Long = 2,
    baseIncentive: Long = 3, dividingThreshold: Long = 100000,
    formula: Hash = classDigest<DefaultDiff>(Hashers.SHA3512Hasher),
    factories: Factories = defaultFactories,
): CoinbaseParams = factories.coinbaseParamsFactory.create(
    hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold, formula
)

fun generateLedgerParams(
    hashers: Hash = defaultHasher.id, recalculationTime: Long = 1228800000,
    recalculationTrigger: Int = 2048, factories: Factories = defaultFactories,
): LedgerParams =
    factories.ledgerParamsFactory.create(hashers, recalculationTime, recalculationTrigger)

fun generateChainId(
    ledgerHash: Hash, adapter: SchemaProvider, factories: Factories = defaultFactories,
    blockParams: BlockParams = generateBlockParams(),
    coinbaseParams: CoinbaseParams = generateCoinbaseParams(),
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
): ChainId = factories.chainIdFactory.create(
    adapter.id.base64DecodedToHash(), ledgerHash, hashers, encoder, blockParams, coinbaseParams
)

fun immutableTransactionGenerator(
    id: Array<Identity>, factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor, generator: DataGenerator = ::randomData,
): Sequence<ImmutableTransaction> =
    transactionGenerator(id, factories, hashers, encoder, generator)
        .map(MutableTransaction::immutableCopy)

internal fun transactionGenerator(
    id: Array<Identity>, factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor, generator: DataGenerator = ::randomData,
): Sequence<MutableTransaction> = generateSequence {
    val index = random.randomInt(id.size)
    factories.transactionFactory.create(
        id[index].privateKey, id[index].publicKey, PhysicalData(
            GeoCoords(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO), generator()
        ), hashers, encoder
    )
}

fun generateImmutableXTransactions(
    id: Array<Identity>, size: Int, factories: Factories = defaultFactories,
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData,
): MutableSortedList<ImmutableTransaction> =
    immutableTransactionGenerator(id, factories, hashers, encoder, generator)
        .take(size).toMutableSortedList()


fun generateXTransactions(
    id: Array<Identity>, size: Int, factories: Factories = defaultFactories,
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
    generator: DataGenerator = ::randomData,
): MutableSortedList<MutableTransaction> =
    transactionGenerator(id, factories, hashers, encoder, generator)
        .take(size).toMutableSortedList()

fun generateBlock(
    ts: MutableSortedList<MutableTransaction>, ledgerHash: Hash, adapter: SchemaProvider,
    factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
): MutableBlock =
    generateBlockWithChain(ts, generateChainId(ledgerHash, adapter), factories, hashers, encoder)

fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>, chainId: ChainId,
    factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
): MutableBlock = generateBlockWithChain(
    ts, generateCoinbase(chainId.coinbaseParams, factories, hashers, encoder),
    chainId.hash, chainId.blockParams, factories, hashers, encoder
)


fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>, chainHash: Hash, coinbaseParams: CoinbaseParams,
    blockParams: BlockParams, factories: Factories = defaultFactories,
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
): MutableBlock = generateBlockWithChain(
    ts, generateCoinbase(coinbaseParams, factories, hashers, encoder),
    chainHash, blockParams, factories, hashers, encoder
)

fun generateBlockWithChain(
    chainId: ChainId, factories: Factories = defaultFactories,
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
): MutableBlock = generateBlockWithChain(
    mutableSortedListOf(), generateCoinbase(chainId.coinbaseParams, factories, hashers, encoder),
    chainId.hash, chainId.blockParams, factories, hashers, encoder
)

fun generateBlockWithChain(
    chainHash: Hash, coinbaseParams: CoinbaseParams, blockParams: BlockParams,
    factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
): MutableBlock = generateBlockWithChain(
    mutableSortedListOf(), generateCoinbase(coinbaseParams, factories, hashers, encoder),
    chainHash, blockParams, factories, hashers, encoder
)

fun generateBlockWithChain(
    ts: MutableSortedList<MutableTransaction>, coinbase: MutableCoinbase, chainHash: Hash,
    blockParams: BlockParams = generateBlockParams(), factories: Factories = defaultFactories,
    hashers: Hashers = defaultHasher, encoder: BinaryFormat = defaultCbor,
): MutableBlock {
    val merkle = factories.merkleTreeFactory.create(
        hashers, coinbase.coinbaseHeader, ts.toTypedArray()
    )
    val blockHeader = factories.blockHeaderFactory.create(
        chainHash, Hash(random.randomByteArray(32)), blockParams, hashers, encoder, merkle.hash
    )
    return factories.blockFactory.create(blockHeader, coinbase, merkle, ts.indexed())
}

fun generateCoinbase(
    coinbaseParams: CoinbaseParams = generateCoinbaseParams(),
    factories: Factories = defaultFactories, hashers: Hashers = defaultHasher,
    encoder: BinaryFormat = defaultCbor,
): MutableCoinbase {
    val merkle = factories.merkleTreeFactory.create(hashers, arrayOf())
    val header = factories.coinbaseHeaderFactory.create(
        coinbaseParams, hashers, encoder, merkle.hash
    )
    return factories.coinbaseFactory.create(header, merkle, mutableSortedListOf())
}