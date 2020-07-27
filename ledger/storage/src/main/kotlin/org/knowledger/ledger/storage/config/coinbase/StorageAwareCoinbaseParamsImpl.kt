package org.knowledger.ledger.storage.config.coinbase

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageAwareCoinbaseParamsImpl(
    override val coinbaseParams: CoinbaseParams
) : CoinbaseParams by coinbaseParams, StorageAwareCoinbaseParams {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()


    override fun equals(other: Any?): Boolean =
        coinbaseParams == other

    override fun hashCode(): Int =
        coinbaseParams.hashCode()
}