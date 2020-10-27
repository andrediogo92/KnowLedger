package org.knowledger.ledger.adapters

import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.cache.StorageAware

/**
 * EagerStorable stores
 */
internal interface EagerStorable<in T> {
    fun update(element: T, state: StorageState): Outcome<Unit, DataFailure> =
        state.pushAllToStore(element as StorageAware).ok()

    fun store(element: T, state: StorageState): Outcome<Unit, DataFailure>
}