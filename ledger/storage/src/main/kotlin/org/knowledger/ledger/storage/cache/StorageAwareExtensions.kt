package org.knowledger.ledger.storage.cache

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Array<StoragePairs<*>>.replaceUnchecked(index: Int, element: T) {
    (this[index] as StoragePairs<T>).replace(element)
}

