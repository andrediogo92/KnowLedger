package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.QueryFailure

internal interface QueryLoadable<T> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}