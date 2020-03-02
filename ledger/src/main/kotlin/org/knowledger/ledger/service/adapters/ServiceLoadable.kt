package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.LedgerFailure

internal interface ServiceLoadable<T : ServiceClass> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LedgerFailure>
}