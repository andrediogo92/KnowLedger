package org.knowledger.ledger.chain.transactions

import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageSolver
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.storage.ChainId

internal class QueryManager constructor(
    val chainId: ChainId, session: ManagedSession,
    context: PersistenceContext, solver: StorageSolver,
) : AbstractQueryManager(chainId.ledgerHash, session, context, solver)