package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.results.LoadFailure

internal interface HandleLoadable<T> {
    fun load(
        ledgerHash: Hash, element: StorageElement, context: LedgerMagicPair,
    ): Outcome<T, LoadFailure>
}