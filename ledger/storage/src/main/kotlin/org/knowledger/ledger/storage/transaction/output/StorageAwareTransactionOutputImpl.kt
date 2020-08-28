package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageAwareTransactionOutputImpl(
    override val transactionOutput: TransactionOutput,
) : TransactionOutput by transactionOutput, StorageAwareTransactionOutput {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()

    override fun equals(other: Any?): Boolean = transactionOutput == other

    override fun hashCode(): Int = transactionOutput.hashCode()
}