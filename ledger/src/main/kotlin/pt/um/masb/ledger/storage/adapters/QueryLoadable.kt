package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.results.QueryFailure

interface QueryLoadable<T : Any> {
    fun load(
        element: StorageElement
    ): Outcome<T, QueryFailure>
}