package org.knowledger.ledger.storage

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure

internal interface StorageAware<T> : Cloneable {
    var id: StorageID?
    val invalidated: Array<StoragePairs<*>>
    fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure>
}