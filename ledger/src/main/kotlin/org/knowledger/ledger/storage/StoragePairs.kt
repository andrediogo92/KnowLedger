package org.knowledger.ledger.storage

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageBytes
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.crypto.Hash as LedgerHash
import org.knowledger.ledger.data.Payout as LedgerPayout

internal sealed class StoragePairs<T> {
    abstract val key: String
    internal var wrapped: T? = null
    internal var dirty: Boolean = false

    val value: T
        get() = wrapped!!


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StoragePairs<*>) return false

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    abstract fun set(
        element: StorageElement
    ): StorageElement

    fun replace(new: T) {
        wrapped = new
        dirty = true
    }

    protected inline fun invalidate(
        element: StorageElement,
        apply: StorageElement.() -> StorageElement
    ): StorageElement {
        dirty = false
        val elem = element.apply()
        wrapped = null
        return elem
    }

    internal data class LinkedList<T : Hashing>(
        override val key: String,
        internal val adapter: EagerStorable<T>
    ) : StoragePairs<MutableList<StorageID>>() {
        fun add(new: T, session: ManagedSession) {
            if (!value.any {
                    it.element.getHashProperty("hash") == new.hash
                }) {
                value += adapter.persist(new, session).identity
            }
            dirty = true
        }

        fun remove(new: T) {
            value.removeIf {
                it.element.getHashProperty("hash") == new.hash
            }
            dirty = true
        }

        override fun set(
            element: StorageElement
        ): StorageElement {
            val elem = element.setElementListById(key, value)
            dirty = false
            return elem
        }
    }

    internal data class LinkedSet<T : Hashing>(
        override val key: String,
        internal val adapter: EagerStorable<T>
    ) : StoragePairs<MutableSet<StorageID>>() {
        fun add(new: T, session: ManagedSession) {
            if (!value.any {
                    it.element.getHashProperty("hash") == new.hash
                }) {
                value += adapter.persist(new, session).identity
            }
            dirty = true
        }

        fun remove(new: T) {
            value.removeIf {
                it.element.getHashProperty("hash") == new.hash
            }
            dirty = true
        }

        override fun set(
            element: StorageElement
        ): StorageElement {
            val elem = element.setElementSetById(key, value)
            dirty = false
            return elem
        }
    }


    internal data class Linked<T>(
        override val key: String,
        internal val adapter: EagerStorable<T>
    ) : StoragePairs<T>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            element.apply {
                dirty = false
            }

        fun update(
            session: ManagedSession
        ): Outcome<StorageID, UpdateFailure> =
            when (value) {
                is StorageAware ->
                    (value as StorageAware).update(session)
                else -> {
                    adapter.persist(value, session).let {
                        Outcome.Ok(it.identity)
                    }
                }
            }
    }

    internal data class Blob(
        override val key: String
    ) : StoragePairs<StorageBytes>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setStorageBytes(key, value) }
    }

    internal data class Hash(
        override val key: String
    ) : StoragePairs<LedgerHash>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setHashProperty(key, value) }
    }

    internal data class HashList(
        override val key: String
    ) : StoragePairs<List<LedgerHash>>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setHashList(key, value) }
    }

    internal data class HashSet(
        override val key: String
    ) : StoragePairs<Set<LedgerHash>>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setHashSet(key, value) }

    }

    internal data class Payout(
        override val key: String
    ) : StoragePairs<LedgerPayout>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setPayoutProperty(key, value) }
    }

    internal data class Difficulty(
        override val key: String
    ) : StoragePairs<StorageBytes>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setStorageBytes(key, value) }
    }

    internal data class Native(
        override val key: String
    ) : StoragePairs<Any>() {
        override fun set(
            element: StorageElement
        ): StorageElement =
            invalidate(element) { setStorageProperty(key, value) }
    }

}