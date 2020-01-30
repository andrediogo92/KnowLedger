package org.knowledger.ledger.test

import kotlinx.serialization.BinaryFormat
import org.knowledger.collections.toSizedArray
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.*
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.testing.core.random
import org.knowledger.testing.ledger.testEncoder
import org.knowledger.testing.ledger.testHasher
import java.math.BigDecimal

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
        ts.toSortedSet(), coinbase,
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

fun transactionGenerator(
    id: Array<Identity>,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder
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
                TemperatureData(
                    BigDecimal(
                        random.randomDouble() * 100
                    ), TemperatureUnit.Celsius
                )
            ), hasher, encoder
        )
    }
}

fun generateXTransactions(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder
): List<Transaction> =
    transactionGenerator(id, hasher, encoder)
        .take(size)
        .toList()

fun generateXTransactions(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat
): List<Transaction> =
    transactionGenerator(
        arrayOf(id), hasher, encoder
    ).take(size).toList()

fun generateXTransactionsArray(
    id: Array<Identity>,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat = testEncoder
): Array<Transaction> =
    transactionGenerator(
        id, hasher, encoder
    ).toSizedArray(size)

fun generateXTransactionsArray(
    id: Identity,
    size: Int,
    hasher: Hashers = testHasher,
    encoder: BinaryFormat
): Array<Transaction> =
    transactionGenerator(
        arrayOf(id), hasher, encoder
    ).toSizedArray(size)

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
        ts.toSortedSet(), coinbase,
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
        hasher, encoder, formula, coinbaseParams
    )
    return BlockImpl(
        sortedSetOf(), coinbase,
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
    val sets = listOf(
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
    encoder: BinaryFormat = testEncoder,
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


fun Sequence<Transaction>.asTransactions(): List<Transaction> =
    asIterable().asTransactions()

internal fun Iterable<Transaction>.asTransactions(): List<Transaction> =
    map { (it as StorageAwareTransaction).transaction }.toList()