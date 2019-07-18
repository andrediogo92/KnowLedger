package org.knowledger.ledger.storage

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageID
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.Storable
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