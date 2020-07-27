package org.knowledger.ledger.storage.config.coinbase

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareCoinbaseParams : StorageAware, CoinbaseParams {
    val coinbaseParams: CoinbaseParams
}