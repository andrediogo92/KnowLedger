package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun HashedTransactionOutput.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareTransactionOutput ->
            SATransactionOutputStorageAdapter.store(this, session)
        is HashedTransactionOutputImpl ->
            SUTransactionOutputStorageAdapter.store(this, session)
        else -> deadCode()
    }

internal fun StorageElement.loadTransactionOutputByImpl(
    ledgerHash: Hash
): Outcome<HashedTransactionOutput, LoadFailure> =
    SATransactionOutputStorageAdapter.load(ledgerHash, this)