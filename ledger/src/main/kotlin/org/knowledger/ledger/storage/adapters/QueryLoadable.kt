package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.StorageElement
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.results.QueryFailure

interface QueryLoadable<T : Any> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}