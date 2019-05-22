package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.storage.results.QueryResult

interface QueryLoadable<T : Any> {
    fun load(element: StorageElement): QueryResult<T>
}