package org.knowledger.ledger.chain.transactions

import org.knowledger.collections.ObjectPool
import org.knowledger.ledger.adapters.AdaptersCollection
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageSolver
import org.knowledger.ledger.chain.solver.StorageSolverState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.Identity
import org.knowledger.ledger.storage.results.LoadFailure

internal abstract class AbstractQueryManager(
    override val ledgerHash: Hash,
    override val session: ManagedSession,
    override val context: PersistenceContext,
    override val solver: StorageSolver,
) : AdaptersCollection by context, EntityStore, Querying {
    override val statePool: ObjectPool<StorageSolverState> = EntityStore.solverStatePool

    // ------------------------------
    // Identity transaction.
    //
    // ------------------------------
    internal fun getLedgerIdentityByTag(id: String): Outcome<Identity, LoadFailure> =
        identityStorageAdapter.let {
            val query = UnspecificQuery(
                """ SELECT 
                    FROM ${it.id}
                    WHERE id = :id
                """.trimIndent(), mapOf("id" to id)
            )
            queryUniqueResult(query, it)
        }
}