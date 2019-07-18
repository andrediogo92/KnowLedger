package org.knowledger.ledger.storage

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.Storable
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

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> StorageAware<T>.simpleUpdate(
    invalidatedMap: MutableMap<String, Any>
): Outcome<StorageID, UpdateFailure> =
    commonUpdate { elem ->
        invalidatedMap.forEach {
            elem.setStorageProperty(it.key, it.value)
        }
        invalidatedMap.clear()
        Outcome.Ok(id!!)
    }

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T, U> StorageAware<T>.updateLinked(
    session: NewInstanceSession,
    key: String, toUpdate: U,
    invalidatedMap: MutableMap<String, Any>,
    adapter: Storable<U>
): Outcome<StorageID, UpdateFailure> =
    if (invalidated.containsKey(key)) {
        when (toUpdate) {
            is StorageAware<*> ->
                toUpdate.update(session)
            else -> {
                adapter.store(toUpdate, session).let {
                    invalidatedMap.replace(key, it)
                    Outcome.Ok(it.identity)
                }
            }
        }
    } else {
        Outcome.Ok(id!!)
    }