package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareCoinbaseHeader : StorageAware,
                                                MutableHashedCoinbaseHeader {
    val coinbaseHeader: MutableHashedCoinbaseHeader
}