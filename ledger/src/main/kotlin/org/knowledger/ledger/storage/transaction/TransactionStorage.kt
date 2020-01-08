package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun HashedTransaction.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareTransaction ->
            SATransactionStorageAdapter.store(this, session)
        is HashedTransactionImpl ->
            SUTransactionStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadTransactionByImpl(
    ledgerHash: Hash
): Outcome<HashedTransaction, LoadFailure> =
    SATransactionStorageAdapter.load(ledgerHash, this)