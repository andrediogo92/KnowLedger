package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.service.results.LoadFailure

internal interface StorageLoadable<T : LedgerContract> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LoadFailure>
}