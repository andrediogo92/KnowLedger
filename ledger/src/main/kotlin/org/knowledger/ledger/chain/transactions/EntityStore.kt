package org.knowledger.ledger.chain.transactions

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import org.knowledger.collections.FixedSizeObjectPool
import org.knowledger.collections.ObjectPool
import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.results.intoQuery
import org.knowledger.ledger.chain.solver.StorageSolver
import org.knowledger.ledger.chain.solver.StorageSolverState
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.database.results.QueryFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.results.tryOrQueryUnknownFailure
import org.tinylog.kotlin.Logger

internal interface EntityStore {
    val statePool: ObjectPool<StorageSolverState>
    val session: ManagedSession
    val context: PersistenceContext
    val solver: StorageSolver
    val isClosed get() = session.isClosed

    fun beginTransaction() {
        session.begin()
    }

    fun commitTransaction() {
        session.commit()
    }

    fun rollbackTransaction() {
        session.rollback()
    }

    fun getInstanceSession(): NewInstanceSession = session

    /**
     * Persists an [element] to an active [ManagedSession] in a synchronous manner, in a transaction
     * context.
     *
     * Returns an [Outcome] with a possible [QueryFailure] over a [StorageID].
     */
    fun <T : Any, Store> persistEntity(element: T, storable: Store): Outcome<Unit, QueryFailure>
            where Store : EagerStorable<T>, Store : SchemaProvider =
        tryOrQueryUnknownFailure {
            beginTransaction()
            val state = statePool.lease()
            @Suppress("UNCHECKED_CAST")
            binding<Unit, DataFailure> {
                solver.primeStart(state, storable, element)
                val tracker = solver.resolve(state, context).bind()
                tracker.elements.forEach(session::save)
            }.onSuccess {
                commitTransaction()
            }.mapError {
                rollbackTransaction()
                it.intoQuery()
            }.also {
                state.purge()
                statePool.free(state)
            }
        }

    /**
     * Persists an [element] to an active [ManagedSession] in a synchronous manner.
     *
     * Returns an [Outcome] with a possible [QueryFailure] over a [StorageID].
     */
    fun <T : Any, Store> persistEntity(
        element: T, storable: Store, cluster: String,
    ): Outcome<Unit, QueryFailure> where Store : EagerStorable<T>, Store : SchemaProvider {
        Logger.warn { "Clusters are ignored in current version: $cluster" }
        //Don't use clusters for now
        return persistEntity(element, storable)
    }

    companion object {
        internal val solverStatePool by lazy {
            FixedSizeObjectPool(8) {
                val size = AdapterIds.values().size
                StorageSolverState(
                    ArrayDeque(size), ArrayList(size), ArrayList(size), ArrayDeque(size)
                )
            }
        }
    }
}
