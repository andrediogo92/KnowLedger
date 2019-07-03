package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.StorageElement
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.service.results.LoadFailure

interface StorageLoadable<T : LedgerContract> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LoadFailure>
}