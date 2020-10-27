package org.knowledger.ledger.chain.solver.trackers

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs

internal sealed class StorageTracker {
    internal enum class Status {
        InCollection,
        Agnostic;
    }

    internal data class Error(val key: String) : StorageTracker()
    internal data class Start(
        val key: String, val element: StorageElement, val status: Status, val lock: Locking,
    ) : StorageTracker()

    internal data class Collection(
        val key: String, val tracker: CollectionTracker,
        val status: Status = Status.Agnostic,
    ) : StorageTracker()

    internal data class Element(val store: StoragePairs<*>) : StorageTracker()
}