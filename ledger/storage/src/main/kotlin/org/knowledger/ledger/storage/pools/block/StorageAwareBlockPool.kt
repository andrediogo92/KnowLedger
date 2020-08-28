package org.knowledger.ledger.storage.pools.block

import org.knowledger.ledger.storage.cache.StorageAware

interface StorageAwareBlockPool : MutableBlockPool, StorageAware