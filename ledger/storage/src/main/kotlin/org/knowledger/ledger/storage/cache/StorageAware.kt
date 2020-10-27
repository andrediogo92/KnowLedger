package org.knowledger.ledger.storage.cache

import org.knowledger.ledger.database.StorageElement

interface StorageAware {
    val lock: Locking
    var id: StorageElement?
    val invalidated: Array<StoragePairs<*>>

    fun clearInvalidated() {
        invalidated.filter(StoragePairs<*>::invalidated).forEach(StoragePairs<*>::resetValue)
    }

}