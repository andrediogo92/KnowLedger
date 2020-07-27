package org.knowledger.ledger.storage.config.block

import org.knowledger.ledger.storage.cache.StorageAware

interface StorageAwareBlockParams : BlockParams, StorageAware {
    val blockParams: BlockParams
}