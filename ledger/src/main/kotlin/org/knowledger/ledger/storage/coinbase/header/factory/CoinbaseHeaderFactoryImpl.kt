package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.CoinbaseHeaderImpl
import org.knowledger.ledger.storage.coinbase.header.HashedCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.HashedCoinbaseHeaderImpl

internal object CoinbaseHeaderFactoryImpl : CoinbaseHeaderFactory {
    private fun generateHash(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hasher: Hashers,
        encoder: BinaryFormat
    ): Hash =
        CoinbaseHeaderImpl(
            coinbaseParams, merkleRoot, payout,
            difficulty, blockheight, extraNonce
        ).calculateHash(hasher, encoder)

    override fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hasher: Hashers,
        encoder: BinaryFormat
    ): HashedCoinbaseHeaderImpl {
        val hash = generateHash(
            coinbaseParams, merkleRoot,
            payout, difficulty, blockheight,
            extraNonce, hasher, encoder
        )
        return create(
            coinbaseParams, merkleRoot,
            payout, difficulty,
            blockheight, extraNonce, hash
        )
    }

    override fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hash: Hash
    ): HashedCoinbaseHeaderImpl =
        HashedCoinbaseHeaderImpl(
            coinbaseParams = coinbaseParams,
            _merkleRoot = merkleRoot, _payout = payout,
            _difficulty = difficulty, _blockheight = blockheight,
            _extraNonce = extraNonce, _hash = hash
        )

    override fun create(
        coinbase: HashedCoinbaseHeader
    ): HashedCoinbaseHeaderImpl = with(coinbase) {
        create(
            coinbaseParams, merkleRoot,
            payout, difficulty, blockheight,
            extraNonce, hash
        )
    }

    override fun create(
        other: MutableCoinbaseHeader
    ): HashedCoinbaseHeaderImpl = with(other) {
        create(
            coinbaseParams, merkleRoot,
            payout, difficulty, blockheight,
            extraNonce, hash
        )
    }

}