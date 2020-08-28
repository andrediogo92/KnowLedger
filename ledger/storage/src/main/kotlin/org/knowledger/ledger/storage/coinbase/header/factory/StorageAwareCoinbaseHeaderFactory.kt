package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.coinbase.header.StorageAwareCoinbaseHeaderImpl

@OptIn(ExperimentalSerializationApi::class)
internal class StorageAwareCoinbaseHeaderFactory(
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory = HashedCoinbaseHeaderFactory(),
) : CoinbaseHeaderFactory {
    private fun createSA(header: MutableCoinbaseHeader): StorageAwareCoinbaseHeaderImpl =
        StorageAwareCoinbaseHeaderImpl(header)

    override fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat, merkleRoot: Hash,
        payout: Payout, blockheight: Long, difficulty: Difficulty, extraNonce: Long,
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            coinbaseParams, hashers, encoder, merkleRoot,
            payout, blockheight, difficulty, extraNonce
        )
    )

    override fun create(
        hash: Hash, merkleRoot: Hash, payout: Payout, blockheight: Long,
        difficulty: Difficulty, extraNonce: Long, coinbaseParams: CoinbaseParams,
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            hash, merkleRoot, payout, blockheight, difficulty, extraNonce, coinbaseParams
        )
    )

    override fun create(coinbase: CoinbaseHeader): StorageAwareCoinbaseHeaderImpl =
        createSA(coinbaseHeaderFactory.create(coinbase))


    override fun create(other: MutableCoinbaseHeader): StorageAwareCoinbaseHeaderImpl =
        createSA(coinbaseHeaderFactory.create(other))

}