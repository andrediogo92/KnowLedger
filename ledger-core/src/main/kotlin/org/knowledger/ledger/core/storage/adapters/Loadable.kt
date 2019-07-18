package org.knowledger.ledger.core.storage.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.results.DataFailure

/**
 * Describes the necessary contract for loading a [LedgerData]
 * from a storage element backed by persistent storage.
 */
interface Loadable<T : LedgerData> {
    fun load(
        element: StorageElement
    ): Outcome<T, DataFailure>
}