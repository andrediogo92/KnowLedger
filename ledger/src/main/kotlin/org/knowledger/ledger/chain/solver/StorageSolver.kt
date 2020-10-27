package org.knowledger.ledger.chain.solver

import org.knowledger.ledger.adapters.AdaptersCollection
import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.chain.solver.trackers.ResultTracker
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome

internal interface StorageSolver {
    fun resolve(
        state: StorageSolverState, adapters: AdaptersCollection,
    ): Outcome<ResultTracker, DataFailure>

    fun <T : Any, Store> primeStart(state: StorageSolverState, storable: Store, element: T)
            where Store : EagerStorable<T>, Store : SchemaProvider
}