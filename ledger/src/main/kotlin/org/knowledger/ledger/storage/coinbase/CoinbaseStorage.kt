package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Coinbase

internal fun Coinbase.store(
    session: ManagedSession
): StorageElement =
    when (this) {
        is StorageAwareCoinbase ->
            SACoinbaseStorageAdapter.store(this, session)
        is HashedCoinbaseImpl ->
            SUHCoinbaseStorageAdapter.store(this, session)
        else -> deadCode()
    }


internal fun StorageElement.loadCoinbaseByImpl(
    ledgerHash: Hash
): Outcome<Coinbase, LoadFailure> =
    SACoinbaseStorageAdapter.load(ledgerHash, this)