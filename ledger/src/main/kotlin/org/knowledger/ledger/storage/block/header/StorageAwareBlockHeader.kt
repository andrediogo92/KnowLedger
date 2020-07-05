package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareBlockHeader : MutableHashedBlockHeader, StorageAware {
    val blockHeader: MutableHashedBlockHeader
}