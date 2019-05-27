package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.DataFailure

/**
 * Describes the necessary contract for loading a [BlockChainData]
 * from a storage element backed by persistent storage.
 */
interface Loadable<T : BlockChainData> {
    fun load(
        element: StorageElement
    ): Outcome<T, DataFailure>
}