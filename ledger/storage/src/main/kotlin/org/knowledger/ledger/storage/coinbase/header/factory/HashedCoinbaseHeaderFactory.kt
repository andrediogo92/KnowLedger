package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
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

@OptIn(ExperimentalSerializationApi::class)
internal class HashedCoinbaseHeaderFactory : CoinbaseHeaderFactory {
    private fun generateHash(
        merkleRoot: Hash, payout: Payout, blockheight: Long, difficulty: Difficulty,
        extraNonce: Long, coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): Hash = CoinbaseHeaderImpl(
        merkleRoot, payout, blockheight, difficulty, extraNonce, coinbaseParams
    ).calculateHash(hashers, encoder)

    override fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat, merkleRoot: Hash,
        payout: Payout, blockheight: Long, difficulty: Difficulty, extraNonce: Long,
    ): HashedCoinbaseHeaderImpl {
        val hash = generateHash(
            merkleRoot, payout, blockheight, difficulty,
            extraNonce, coinbaseParams, hashers, encoder
        )
        return create(hash, merkleRoot, payout, blockheight, difficulty, extraNonce, coinbaseParams)
    }

    override fun create(
        hash: Hash, merkleRoot: Hash, payout: Payout, blockheight: Long,
        difficulty: Difficulty, extraNonce: Long, coinbaseParams: CoinbaseParams,
    ): HashedCoinbaseHeaderImpl = HashedCoinbaseHeaderImpl(
        hash, merkleRoot, payout, blockheight, difficulty, extraNonce, coinbaseParams
    )

    override fun create(coinbase: CoinbaseHeader): HashedCoinbaseHeaderImpl =
        with(coinbase) {
            create(hash, merkleRoot, payout, blockheight, difficulty, extraNonce, coinbaseParams)
        }

    override fun create(other: MutableCoinbaseHeader): HashedCoinbaseHeaderImpl =
        create(other as CoinbaseHeader)

}