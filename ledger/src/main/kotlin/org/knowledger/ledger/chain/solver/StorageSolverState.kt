package org.knowledger.ledger.chain.solver

import com.github.michaelbull.result.Ok
import org.knowledger.ledger.chain.solver.trackers.CollectionTracker
import org.knowledger.ledger.chain.solver.trackers.IndexTracker
import org.knowledger.ledger.chain.solver.trackers.StorageTracker
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.LockState
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageSolverState(
    private val storageStack: ArrayDeque<StorageTracker>,
    private val resolvedStack: MutableList<StorageElement>,
    private val lockingStack: MutableList<StorageAware>,
    private val collectionStack: ArrayDeque<IndexTracker>,
) : StorageState {
    fun noElementsToStore(): Boolean = storageStack.isEmpty()
    fun hasElementsToStore(): Boolean = storageStack.isNotEmpty()
    fun purge() {
        storageStack.clear()
        resolvedStack.clear()
        lockingStack.clear()
        collectionStack.clear()
    }


    override fun pushToStore(store: StoragePairs<*>) {
        storageStack.addLast(StorageTracker.Element(store))
    }

    private fun pushBegin(
        key: String, element: StorageElement, status: StorageTracker.Status,
        lock: Locking,
    ) {
        collectionStack.addLast(IndexTracker(storageStack.size))
        val tracker = StorageTracker.Start(key, element, status, lock)
        storageStack.addLast(tracker)
    }

    override fun pushBeginToStore(
        key: String, element: StorageElement, status: StorageTracker.Status, lockState: LockState,
    ) {
        pushBegin(key, element, status, BooleanLocking(lockState))
    }

    override fun pushBeginAndLock(
        key: String, element: StorageElement, status: StorageTracker.Status, aware: StorageAware,
    ) {
        aware.lock.lock()
        lockingStack.add(aware)
        pushBegin(key, element, status, BooleanLocking(LockState.Unlocked))
    }

    override fun pushCollectionToStore(
        key: String, type: CollectionTracker.Type,
    ): IndexTracker {
        val tracker = CollectionTracker(type, when (type) {
            CollectionTracker.Type.List -> mutableListOf()
            CollectionTracker.Type.Set -> mutableSetOf()
        })
        val index = IndexTracker(storageStack.size)
        storageStack.addLast(StorageTracker.Collection(key, tracker))
        return index
    }

    fun resolveNext(): StorageTracker = storageStack.last()
    fun advanceNext(): StorageTracker = storageStack.removeLast()


    fun addResolvedElement(element: StorageElement) {
        resolvedStack.add(element)
    }

    fun resolvedIterator(): Iterator<StorageElement> = resolvedStack.iterator()

    fun unlockElements() {
        lockingStack.forEach {
            it.clearInvalidated()
            it.lock.release()
        }
    }

    fun trackCollection(index: IndexTracker) {
        collectionStack.addLast(index)
    }

    fun setToStart(tracker: StorageTracker.Start): Outcome<StorageElement?, DataFailure> =
        when (val start = storageStack[collectionStack.last().index]) {
            is StorageTracker.Start -> {
                if (tracker.lock.state == LockState.Unlocked) {
                    start.element.setLinked(tracker.key, tracker.element)
                    tracker.element.ok()
                } else {
                    Ok(null)
                }
            }
            is StorageTracker.Collection -> {
                start.tracker.addNew(tracker.element)
                Ok(null)
            }
            is StorageTracker.Error ->
                DataFailure.UnexpectedClass("Stack index points to Error: $start").err()
            is StorageTracker.Element ->
                DataFailure.UnexpectedClass("Stack index points to Element: $start").err()

        }

    fun setToCollection(): Outcome<StorageElement, DataFailure> {
        val tracker = storageStack[collectionStack.last().index]
        return (tracker as? StorageTracker.Start)?.element?.ok() ?: DataFailure.UnexpectedClass(
            "Collection tracker points to: $tracker").err()
    }

    fun extractStart(): StorageElement =
        (storageStack[collectionStack.last().index] as StorageTracker.Start).element


    fun removeLinked(): IndexTracker = collectionStack.removeLast()
}