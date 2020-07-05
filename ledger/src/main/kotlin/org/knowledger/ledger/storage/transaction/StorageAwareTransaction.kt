package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareTransaction : StorageAware,
                                             MutableHashedTransaction {
    val transaction: MutableHashedTransaction
}