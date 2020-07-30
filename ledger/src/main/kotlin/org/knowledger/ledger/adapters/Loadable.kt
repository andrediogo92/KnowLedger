package org.knowledger.ledger.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.PersistenceContext

internal interface Loadable<out T, out R : Failure> {
    fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<T, R>
}