package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.results.QueryFailure

interface QueryLoadable<T : Any> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}