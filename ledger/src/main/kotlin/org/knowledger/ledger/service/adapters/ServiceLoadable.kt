package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.LedgerFailure

interface ServiceLoadable<T : ServiceClass> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LedgerFailure>
}