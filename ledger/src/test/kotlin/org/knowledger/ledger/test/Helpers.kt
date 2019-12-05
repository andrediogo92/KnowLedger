package org.knowledger.ledger.test

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hasher
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
import org.knowledger.testing.ledger.encoder
import org.knowledger.testing.ledger.testHasher
import java.math.BigDecimal

fun generateChainId(
    hasher: Hasher = Hashers.DEFAULT_HASHER
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
            Hash(random.randomByteArray(32)),
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
                        random.randomDouble() * 100
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
            Hash(random.randomByteArray(32)),
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