package org.knowledger.ledger.storage.config.coinbase.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.config.coinbase.StorageAwareCoinbaseParamsImpl

internal class StorageAwareCoinbaseParamsFactory(
    private val factory: CoinbaseParamsFactory = CoinbaseParamsFactoryImpl()
) : CoinbaseParamsFactory {
    private fun createSA(
        coinbaseParams: CoinbaseParams
    ): StorageAwareCoinbaseParamsImpl =
        StorageAwareCoinbaseParamsImpl(coinbaseParams)

    override fun create(
        hashSize: Int, timeIncentive: Long, valueIncentive: Long,
        baseIncentive: Long, dividingThreshold: Long, formula: Hash
    ): StorageAwareCoinbaseParamsImpl = createSA(
        factory.create(
            hashSize, timeIncentive, valueIncentive,
            baseIncentive, dividingThreshold, formula
        )
    )

    override fun create(other: CoinbaseParams): StorageAwareCoinbaseParamsImpl =
        createSA(factory.create(other))
}