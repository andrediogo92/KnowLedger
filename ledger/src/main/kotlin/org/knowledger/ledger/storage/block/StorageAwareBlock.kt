package org.knowledger.ledger.storage.block

import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareBlock : MutableBlock, StorageAware {
    val block: MutableBlock
}