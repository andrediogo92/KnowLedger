package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.commonUpdate

internal data class StorageAwareTransaction(
    internal val transaction: HashedTransactionImpl
) : HashedTransaction by transaction, StorageAware<HashedTransaction> {
    override var id: StorageID? = null

    override val invalidated: Array<StoragePairs<*>>
        get() = emptyArray()

    override fun update(session: ManagedSession): Outcome<StorageID, UpdateFailure> =
        commonUpdate {
            Outcome.Ok(it.identity)
        }
}