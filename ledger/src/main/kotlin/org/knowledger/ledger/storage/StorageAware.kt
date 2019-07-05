package org.knowledger.ledger.storage

import org.knowledger.common.database.StorageID

interface StorageAware<T> {
    var id: StorageID?
}