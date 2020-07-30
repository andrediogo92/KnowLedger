package org.knowledger.ledger.adapters

import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushAllToStore
import org.knowledger.ledger.storage.cache.StorageAware

/**
 * EagerStorable stores
 */
internal interface EagerStorable<in T> {
    fun update(
        element: T, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            solver.pushAllToStore(element as StorageAware).ok()
        }

    fun store(
        element: T, solver: StorageSolver
    ): Outcome<Unit, DataFailure>
}