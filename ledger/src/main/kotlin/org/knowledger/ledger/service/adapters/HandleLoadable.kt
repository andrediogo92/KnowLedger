package org.knowledger.ledger.service.adapters

import org.knowledger.common.database.StorageElement
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.service.handles.LedgerHandle

interface HandleLoadable {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure>
}