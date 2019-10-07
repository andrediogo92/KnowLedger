package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig

internal interface HandleLoadable {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure>
}