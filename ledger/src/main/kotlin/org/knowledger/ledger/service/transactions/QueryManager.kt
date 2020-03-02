package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession

internal class QueryManager constructor(
    ledgerHash: Hash,
    session: ManagedSession,
    adapterManager: AdapterManager,
    private val chainHash: Hash
) : AbstractQueryManager(ledgerHash, session, adapterManager)