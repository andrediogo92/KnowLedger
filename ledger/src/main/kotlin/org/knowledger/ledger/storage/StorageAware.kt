package org.knowledger.ledger.storage

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure

internal interface StorageAware<T> : Cloneable {
    var id: StorageID?
    val invalidated: List<StoragePairs>
    fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure>
}