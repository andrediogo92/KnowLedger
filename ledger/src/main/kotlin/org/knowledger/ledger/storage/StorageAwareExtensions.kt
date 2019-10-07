package org.knowledger.ledger.storage

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.reduce
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
    invalidatedMap: Array<StoragePairs<*>>
): Outcome<StorageID, UpdateFailure> =
    commonUpdate { elem ->
        invalidatedMap.forEach {
            if (it.dirty) {
                it.set(elem)
            }
        }
        Outcome.Ok(id!!)
    }

internal fun <T> StorageAware<T>.updateLinked(
    session: ManagedSession,
    invalidatedMap: Array<StoragePairs<*>>
): Outcome<StorageID, UpdateFailure> =
    commonUpdate { elem ->
        for (it in invalidatedMap) {
            if (it.dirty) {
                when (it) {
                    is StoragePairs.Linked<*> -> {
                        it.update(session).reduce(
                            { id ->
                                elem.setLinkedID(it.key, id)
                            }, {
                                return@commonUpdate Outcome.Error(it)
                            }
                        )
                    }
                    else -> it.set(elem)
                }
            }
        }
        Outcome.Ok(elem.identity)
    }

@Suppress("UNCHECKED_CAST")
internal fun <T> Array<StoragePairs<*>>.replace(
    index: Int,
    element: T
) {
    (this[index] as StoragePairs<T>).replace(element)
}