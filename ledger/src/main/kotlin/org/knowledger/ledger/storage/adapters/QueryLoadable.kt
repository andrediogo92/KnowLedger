package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.results.QueryFailure

internal interface QueryLoadable<T> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}