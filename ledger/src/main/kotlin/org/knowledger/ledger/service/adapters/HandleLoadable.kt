package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.handles.LedgerHandle

interface HandleLoadable {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure>
}