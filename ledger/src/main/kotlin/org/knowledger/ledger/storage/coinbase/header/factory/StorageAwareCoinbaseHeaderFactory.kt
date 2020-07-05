package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.coinbase.header.StorageAwareCoinbaseHeaderImpl

internal class StorageAwareCoinbaseHeaderFactory(
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory =
        CoinbaseHeaderFactoryImpl
) : CoinbaseHeaderFactory {
    private fun createSA(
        header: MutableCoinbaseHeader
    ): StorageAwareCoinbaseHeaderImpl =
        StorageAwareCoinbaseHeaderImpl(
            header, arrayOf(
                StoragePairs.Native("extraNonce"),
                StoragePairs.Hash("hash"),
                StoragePairs.Native("blockheight"),
                StoragePairs.Difficulty("difficulty")
            )
        )

    override fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hasher: Hashers,
        encoder: BinaryFormat
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            coinbaseParams, merkleRoot, payout,
            difficulty, blockheight, extraNonce,
            hasher, encoder
        )
    )

    override fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hash: Hash
    ): StorageAwareCoinbaseHeaderImpl = createSA(
        coinbaseHeaderFactory.create(
            coinbaseParams, merkleRoot,
            payout, difficulty, blockheight,
            extraNonce, hash
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