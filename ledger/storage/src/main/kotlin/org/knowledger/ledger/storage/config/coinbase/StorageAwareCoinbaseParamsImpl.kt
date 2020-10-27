package org.knowledger.ledger.storage.config.coinbase

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageAwareCoinbaseParamsImpl(
    override val coinbaseParams: CoinbaseParams,
) : CoinbaseParams by coinbaseParams, StorageAwareCoinbaseParams {
    override val lock: Locking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()

    override fun equals(other: Any?): Boolean = coinbaseParams == other

    override fun hashCode(): Int = coinbaseParams.hashCode()
}