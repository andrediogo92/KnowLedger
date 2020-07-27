package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.coinbase.header.StorageAwareCoinbaseHeaderImpl

internal class StorageAwareCoinbaseHeaderFactory(
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory = HashedCoinbaseHeaderFactory()
) : CoinbaseHeaderFactory {
    private fun createSA(
        header: MutableCoinbaseHeader
    ): StorageAwareCoinbaseHeaderImpl =
        StorageAwareCoinbaseHeaderImpl(header)

    override fun create(
        merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams, hasher: Hashers,
        encoder: BinaryFormat, payout: Payout,
        difficulty: Difficulty, extraNonce: Long
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            merkleRoot, blockheight, coinbaseParams,
            hasher, encoder, payout, difficulty, extraNonce
        )
    )

    override fun create(
        hash: Hash, merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams, payout: Payout,
        difficulty: Difficulty, extraNonce: Long
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            hash, merkleRoot, blockheight, coinbaseParams,
            payout, difficulty, extraNonce
        )
    )

    override fun create(
        coinbase: CoinbaseHeader
    ): StorageAwareCoinbaseHeaderImpl =
        createSA(coinbaseHeaderFactory.create(coinbase))


    override fun create(
        other: MutableCoinbaseHeader
    ): StorageAwareCoinbaseHeaderImpl =
        createSA(coinbaseHeaderFactory.create(other))

}