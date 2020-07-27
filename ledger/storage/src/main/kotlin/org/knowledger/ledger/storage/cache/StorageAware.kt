package org.knowledger.ledger.storage.cache

import org.knowledger.ledger.database.StorageElement

interface StorageAware {
    var id: StorageElement?
    val invalidated: Array<StoragePairs<*>>
}