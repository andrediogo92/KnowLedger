package org.knowledger.ledger.storage

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageID
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure

internal interface StorageAware<T> : Cloneable {
    var id: StorageID?
    val invalidated: Map<String, Any>
    fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure>
}