package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.commonUpdate
import org.knowledger.ledger.storage.replace

internal class StorageAwareTransactionImpl(
    override val transaction: MutableHashedTransaction
) : MutableHashedTransaction by transaction,
    StorageAwareTransaction {
    override var id: StorageID? = null

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Native("index")
        )

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        commonUpdate {
            Outcome.Ok(it.identity)
        }

    override fun markIndex(index: Int) {
        if (index != this.index) {
            transaction.markIndex(index)
            if (id != null) {
                invalidated.replace(0, index)
            }
        }
    }

    override fun equals(other: Any?): Boolean =
        transaction == other

    override fun hashCode(): Int =
        transaction.hashCode()
}