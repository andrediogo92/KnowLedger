package org.knowledger.ledger.service.adapters

import org.knowledger.common.database.StorageElement
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.results.LedgerFailure

interface ServiceLoadable<T : ServiceClass> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LedgerFailure>
}