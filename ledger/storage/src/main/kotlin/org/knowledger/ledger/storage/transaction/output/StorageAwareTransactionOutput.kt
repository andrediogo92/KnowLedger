package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareTransactionOutput : StorageAware, TransactionOutput {
    val transactionOutput: TransactionOutput
}