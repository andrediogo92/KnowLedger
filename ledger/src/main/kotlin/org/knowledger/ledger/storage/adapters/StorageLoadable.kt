package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.LoadFailure

internal interface StorageLoadable<T : LedgerContract> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LoadFailure>
}