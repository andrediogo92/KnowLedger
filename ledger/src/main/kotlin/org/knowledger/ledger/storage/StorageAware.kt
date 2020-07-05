package org.knowledger.ledger.storage

import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.reduce
import org.knowledger.ledger.service.results.UpdateFailure

internal interface StorageAware : Cloneable {
    var id: StorageID?
    val invalidated: Array<StoragePairs<*>>
    fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure>

    fun simpleUpdate(
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

    fun updateLinked(
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

}