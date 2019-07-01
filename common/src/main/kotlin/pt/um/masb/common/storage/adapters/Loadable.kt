package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.DataFailure

/**
 * Describes the necessary contract for loading a [LedgerData]
 * from a storage element backed by persistent storage.
 */
interface Loadable<T : LedgerData> {
    fun load(
        element: StorageElement
    ): Outcome<T, DataFailure>
}