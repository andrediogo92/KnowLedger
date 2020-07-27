package org.knowledger.ledger.storage.cache

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.AdapterIds

sealed class StoragePairs<T : Any> {
    abstract val key: String
    internal var value: T? = null
    private var dirty: Boolean = false

    val element: T
        get() = value!!

    val invalidated: Boolean
        get() = dirty

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoragePairs<*>) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    fun replace(new: T) {
        value = new
        dirty = true
    }

    fun invalidate() {
        dirty = true
    }

    fun reset() {
        dirty = false
    }

    fun resetValue() {
        value = null
        dirty = false
    }


    data class LinkedList<T : Comparable<T>>(
        override val key: String,
        override val adapterId: AdapterIds
    ) : LinkedPairs, StoragePairs<MutableSortedList<T>>()

    data class LinkedSet<T>(
        override val key: String,
        override val adapterId: AdapterIds
    ) : LinkedPairs, StoragePairs<MutableSet<T>>()

    data class Linked<T : Any>(
        override val key: String,
        override val adapterId: AdapterIds
    ) : LinkedPairs, StoragePairs<T>()

    data class Blob(
        override val key: String
    ) : StoragePairs<ByteArray>()

    data class LinkedHash(
        override val key: String
    ) : StoragePairs<Hash>()

    data class HashList(
        override val key: String
    ) : StoragePairs<List<Hash>>()

    data class HashSet(
        override val key: String
    ) : StoragePairs<Set<Hash>>()

    data class LinkedPayout(
        override val key: String
    ) : StoragePairs<Payout>()

    data class LinkedDifficulty(
        override val key: String
    ) : StoragePairs<Difficulty>()

    data class Native(
        override val key: String
    ) : StoragePairs<Any>()

}