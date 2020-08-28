package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwarePoolTransaction : PoolTransaction, StorageAware