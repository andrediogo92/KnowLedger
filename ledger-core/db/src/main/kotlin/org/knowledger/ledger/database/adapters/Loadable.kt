package org.knowledger.ledger.database.adapters

import org.knowledger.ledger.core.base.data.LedgerData
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome

/**
 * Describes the necessary contract for loading a [LedgerData]
 * from a storage element backed by persistent storage.
 */
interface Loadable<T : LedgerData> {
    fun load(
        element: StorageElement
    ): Outcome<T, DataFailure>
}