package org.knowledger.ledger.storage.block

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareBlock : MutableBlock,
                                       StorageAware {
    val block: MutableBlock
}