package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal class StorageAwareTransactionImpl(
    override val transaction: MutableHashedTransaction,
) : MutableHashedTransaction by transaction, StorageAwareTransaction {
    override val lock: Locking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(StoragePairs.Native("index"))

    override fun markIndex(index: Int) {
        if (index != this.index) {
            transaction.markIndex(index)
            if (id != null) {
                invalidated.replaceUnchecked(0, index)
            }
        }
    }

    override fun equals(other: Any?): Boolean = transaction == other

    override fun hashCode(): Int = transaction.hashCode()
}