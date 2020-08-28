package org.knowledger.ledger.storage.config.coinbase.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.config.coinbase.ImmutableCoinbaseParams

internal class CoinbaseParamsFactoryImpl : CoinbaseParamsFactory {
    override fun create(
        hashSize: Int, timeIncentive: Long, valueIncentive: Long,
        baseIncentive: Long, dividingThreshold: Long, formula: Hash,
    ): ImmutableCoinbaseParams = ImmutableCoinbaseParams(
        hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold, formula
    )

    override fun create(other: CoinbaseParams): ImmutableCoinbaseParams =
        with(other) {
            create(
                hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold, formula
            )
        }
}