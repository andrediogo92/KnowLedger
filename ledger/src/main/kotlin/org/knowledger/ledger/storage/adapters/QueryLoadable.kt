package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome

internal interface QueryLoadable<T> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}