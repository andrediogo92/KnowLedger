package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.coinbase.header.CoinbaseHeaderImpl
import org.knowledger.ledger.storage.coinbase.header.HashedCoinbaseHeaderImpl

internal class HashedCoinbaseHeaderFactory : CoinbaseHeaderFactory {
    private fun generateHash(
        merkleRoot: Hash, payout: Payout, blockheight: Long,
        difficulty: Difficulty, extraNonce: Long,
        coinbaseParams: CoinbaseParams,
        hasher: Hashers, encoder: BinaryFormat
    ): Hash = CoinbaseHeaderImpl(
        merkleRoot, payout, blockheight, difficulty,
        extraNonce, coinbaseParams
    ).calculateHash(hasher, encoder)

    override fun create(
        merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams, hasher: Hashers,
        encoder: BinaryFormat, payout: Payout,
        difficulty: Difficulty, extraNonce: Long
    ): HashedCoinbaseHeaderImpl {
        val hash = generateHash(
            merkleRoot, payout, blockheight, difficulty,
            extraNonce, coinbaseParams, hasher, encoder
        )
        return create(
            hash, merkleRoot, blockheight, coinbaseParams,
            payout, difficulty, extraNonce
        )
    }

    override fun create(
        hash: Hash, merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams, payout: Payout,
        difficulty: Difficulty, extraNonce: Long
    ): HashedCoinbaseHeaderImpl = HashedCoinbaseHeaderImpl(
        hash, merkleRoot, payout, blockheight,
        difficulty, extraNonce, coinbaseParams
    )

    override fun create(
        coinbase: CoinbaseHeader
    ): HashedCoinbaseHeaderImpl = with(coinbase) {
        create(
            hash, merkleRoot, blockheight, coinbaseParams,
            payout, difficulty, extraNonce
        )
    }

    override fun create(
        other: MutableCoinbaseHeader
    ): HashedCoinbaseHeaderImpl =
        create(other as CoinbaseHeader)

}