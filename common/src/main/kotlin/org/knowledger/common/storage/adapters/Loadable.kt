package org.knowledger.common.storage.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.StorageElement
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.results.DataFailure

/**
 * Describes the necessary contract for loading a [LedgerData]
 * from a storage element backed by persistent storage.
 */
interface Loadable<T : LedgerData> {
    fun load(
        element: StorageElement
    ): Outcome<T, DataFailure>
}