package org.knowledger.ledger.storage

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.checkSealed
import org.knowledger.ledger.service.results.UpdateFailure

internal inline fun <T> StorageAware<T>.commonUpdate(
    runUpdate: (StorageElement) -> Outcome<StorageID, UpdateFailure>
): Outcome<StorageID, UpdateFailure> =
    if (id == null) {
        Outcome.Error(
            UpdateFailure.NotYetStored
        )
    } else {
        runUpdate(id!!.element)
    }

internal fun <T> StorageAware<T>.simpleUpdate(
    invalidatedMap: MutableList<StoragePairs>
): Outcome<StorageID, UpdateFailure> =
    commonUpdate { elem ->
        invalidatedMap.forEach {
            when (it.value) {
                is StoragePairs.Element.Blob ->
                    elem.setStorageBytes(
                        it.key,
                        (it.value as StoragePairs.Element.Blob).blob
                    )
                is StoragePairs.Element.Hash ->
                    elem.setHashProperty(
                        it.key,
                        (it.value as StoragePairs.Element.Hash).hash
                    )
                is StoragePairs.Element.HashList ->
                    elem.setHashList(
                        it.key,
                        (it.value as StoragePairs.Element.HashList).hashList
                    )
                is StoragePairs.Element.HashSet ->
                    elem.setHashSet(
                        it.key,
                        (it.value as StoragePairs.Element.HashSet).hashSet
                    )
                is StoragePairs.Element.Payout ->
                    elem.setPayoutProperty(
                        it.key,
                        (it.value as StoragePairs.Element.Payout).payout
                    )
                is StoragePairs.Element.Difficulty ->
                    elem.setStorageBytes(
                        it.key,
                        (it.value as StoragePairs.Element.Difficulty).difficulty
                    )
                is StoragePairs.Element.Native ->
                    elem.setStorageProperty(
                        it.key,
                        (it.value as StoragePairs.Element.Native).any
                    )
            }.checkSealed()
        }
        invalidatedMap.clear()
        Outcome.Ok(id!!)
    }

internal fun <T, U> StorageAware<T>.updateLinked(
    session: ManagedSession,
    key: String, toUpdate: U,
    invalidatedMap: MutableList<StoragePairs>,
    adapter: EagerStorable<U>
): Outcome<StorageID, UpdateFailure> {
    val index = invalidated.indexOfFirst { it.key == key }
    return if (index != -1) {
        when (toUpdate) {
            is StorageAware<*> ->
                toUpdate.update(session)
            else -> {
                adapter.persist(toUpdate, session).let {
                    invalidatedMap[index].updateValue(it)
                    Outcome.Ok(it.identity)
                }
            }
        }
    } else {
        Outcome.Ok(id!!)
    }
}

internal fun MutableList<StoragePairs>.replaceInstances(
    keys: Array<String>,
    values: Array<StoragePairs.Element>
): Array<Boolean> {
    val success = Array(keys.size) { false }
    for (pairs in this) {
        if (success.reduce(Boolean::and)) {
            break
        } else {
            val index = keys.indexOf(pairs.key)
            when (index) {
                -1 -> {
                }
                else -> {
                    pairs.updateElement(values[index])
                    success[index] = true
                }
            }
        }
    }
    return success
}

internal fun MutableList<StoragePairs>.replaceInstance(
    key: String,
    value: StoragePairs.Element
): Boolean {
    var updated = false
    for (pairs in this) {
        if (pairs.key == key) {
            pairs.updateElement(value)
            updated = true
            break
        }
    }
    return updated
}

internal fun MutableList<StoragePairs>.addOrReplaceInstance(
    key: String,
    value: StoragePairs.Element
): Boolean =
    replaceInstance(key, value) && add(StoragePairs(key, value))

internal fun MutableList<StoragePairs>.addOrReplaceInstances(
    keys: Array<String>,
    values: Array<StoragePairs.Element>
): Boolean {
    val successes = replaceInstances(keys, values)
    for ((i, success) in successes.withIndex()) {
        if (success) {
            successes[i] = add(StoragePairs(keys[i], values[i]))
        }
    }
    return successes.reduce(Boolean::and)
}
