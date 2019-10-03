package org.knowledger.ledger.storage

import org.knowledger.ledger.core.database.StorageBytes
import org.knowledger.ledger.core.data.Payout as LedgerPayout
import org.knowledger.ledger.core.hash.Hash as LedgerHash

data class StoragePairs(
    val key: String,
    private var _value: Element
) {
    val value: Element
        get() = _value

    internal fun updateWithBlob(blob: StorageBytes) {
        _value = Element.Blob(blob)
    }

    private fun updateDifficulty(difficulty: StorageBytes) {
        _value = Element.Difficulty(difficulty)
    }

    private fun updatePayout(payout: LedgerPayout) {
        _value = Element.Payout(payout)
    }

    private fun updateHash(hash: LedgerHash) {
        _value = Element.Hash(hash)
    }

    private fun updateListHash(hashList: List<LedgerHash>) {
        _value = Element.HashList(hashList)
    }

    private fun updateListSet(hashSet: Set<LedgerHash>) {
        _value = Element.HashSet(hashSet)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun updateValue(value: Any) {
        when (value) {
            is StorageBytes -> updateDifficulty(value)
            is LedgerPayout -> updatePayout(value)
            is LedgerHash -> updateHash(value)
            is List<*> ->
                if (value.isNotEmpty() && value[0] is LedgerHash) {
                    updateListHash(value as List<LedgerHash>)
                } else {
                    _value = Element.Native(value)
                }
            is Set<*> ->
                if (value.isNotEmpty() && value.first() is LedgerHash) {
                    updateListSet(value as Set<LedgerHash>)
                } else {
                    _value = Element.Native(value)
                }
            else -> _value = Element.Native(value)
        }
    }

    internal fun updateElement(element: Element) {
        _value = element
    }

    sealed class Element {
        data class Blob(val blob: StorageBytes) : Element()
        data class Hash(val hash: LedgerHash) : Element()
        data class HashList(val hashList: List<LedgerHash>) : Element()
        data class HashSet(val hashSet: Set<LedgerHash>) : Element()
        data class Payout(val payout: LedgerPayout) : Element()
        data class Difficulty(val difficulty: StorageBytes) : Element()
        data class Native(val any: Any) : Element()
    }
}