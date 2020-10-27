package org.knowledger.ledger.adapters

import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.results.LoadFailure

internal interface Loadable<out T> {
    fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<T, LoadFailure>
}