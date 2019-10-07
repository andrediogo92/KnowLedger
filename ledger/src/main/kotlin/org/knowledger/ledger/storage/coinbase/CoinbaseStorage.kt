package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure

internal fun HashedCoinbase.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareCoinbase ->
            SACoinbaseStorageAdapter.store(this, session)
        is HashedCoinbaseImpl ->
            SUCoinbaseStorageAdapter.store(this, session)
        else -> deadCode()
    }


internal fun StorageElement.loadCoinbaseByImpl(
    ledgerHash: Hash
): Outcome<HashedCoinbase, LoadFailure> =
    SACoinbaseStorageAdapter.load(ledgerHash, this)