package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.coinbase.SACoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.SUCoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase

internal class CoinbaseStorageAdapter(
    private val suCoinbaseStorageAdapter: SUCoinbaseStorageAdapter,
    private val saCoinbaseStorageAdapter: SACoinbaseStorageAdapter
) : LedgerStorageAdapter<Coinbase>,
    SchemaProvider by suCoinbaseStorageAdapter {
    override fun store(
        toStore: Coinbase,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareCoinbase ->
                saCoinbaseStorageAdapter.store(toStore, session)
            is HashedCoinbaseImpl ->
                suCoinbaseStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Coinbase, LoadFailure> =
        saCoinbaseStorageAdapter.load(ledgerHash, element)
}