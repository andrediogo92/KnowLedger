package org.knowledger.ledger.chain.solver

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.chain.solver.trackers.CollectionTracker
import org.knowledger.ledger.chain.solver.trackers.IndexTracker
import org.knowledger.ledger.chain.solver.trackers.StorageTracker
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.cache.LockState
import org.knowledger.ledger.storage.cache.StoragePairs

internal interface StorageState {
    fun pushToStore(store: StoragePairs<*>)
    fun pushBeginToStore(
        key: String, element: StorageElement, status: StorageTracker.Status, lockState: LockState,
    )

    fun pushBeginAndLock(
        key: String, element: StorageElement, status: StorageTracker.Status, aware: StorageAware,
    )

    fun pushCollectionToStore(key: String, type: CollectionTracker.Type): IndexTracker

    fun <T : Any> pushNewLinked(key: String, element: T, adapterId: AdapterIds) {
        pushNew(StoragePairs.Linked(key, adapterId), element)
    }

    fun <T : Comparable<T>> pushNewLinkedList(
        key: String, element: MutableSortedList<T>, adapterId: AdapterIds,
    ) {
        val list = StoragePairs.LinkedList<T>(key, adapterId)
        list.replace(element)
        pushToStore(list)
    }

    fun <T : Any> pushNewLinkedSet(key: String, element: MutableSet<T>, adapterId: AdapterIds) {
        val list = StoragePairs.LinkedSet<T>(key, adapterId)
        list.replace(element)
        pushToStore(list)
    }

    fun <T : Any> pushNewNative(key: String, element: T) {
        pushNew(StoragePairs.Native(key), element)
    }

    fun pushNewBytes(key: String, element: ByteArray) {
        pushNew(StoragePairs.Blob(key), element)
    }

    fun pushNewDifficulty(key: String, element: Difficulty) {
        pushNew(StoragePairs.LinkedDifficulty(key), element)
    }

    fun pushNewPayout(key: String, element: Payout) {
        pushNew(StoragePairs.LinkedPayout(key), element)
    }

    fun pushNewHash(key: String, element: Hash) {
        pushNew(StoragePairs.LinkedHash(key), element)
    }

    fun pushNewHashList(key: String, elements: List<Hash>) {
        pushNew(StoragePairs.HashList(key), elements)
    }

    fun pushNewHashSet(key: String, elements: Set<Hash>) {
        pushNew(StoragePairs.HashSet(key), elements)
    }

    fun pushAllToStore(element: StorageAware) {
        element.invalidated.forEach { pairs ->
            if (pairs.invalidated) pushToStore(pairs)
        }
    }

    fun <T : Any> pushNew(storagePairs: StoragePairs<T>, element: T) {
        storagePairs.replace(element)
        pushToStore(storagePairs)
    }
}