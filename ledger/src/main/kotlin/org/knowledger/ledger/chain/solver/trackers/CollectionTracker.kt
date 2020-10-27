package org.knowledger.ledger.chain.solver.trackers

import org.knowledger.ledger.database.StorageElement

internal data class CollectionTracker(
    val type: Type,
    private val mutableCollection: MutableCollection<StorageElement>,
) {
    enum class Type {
        Set,
        List
    }

    val collection get() = mutableCollection

    fun addNew(element: StorageElement) {
        mutableCollection.add(element)
    }
}