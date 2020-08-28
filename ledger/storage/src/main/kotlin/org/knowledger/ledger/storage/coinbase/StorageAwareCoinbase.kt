package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareCoinbase : MutableCoinbase, StorageAware {
    val coinbase: MutableCoinbase
}