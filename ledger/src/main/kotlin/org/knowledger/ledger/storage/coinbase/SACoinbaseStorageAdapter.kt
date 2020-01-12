package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SACoinbaseStorageAdapter(
    private val suCoinbaseStorageAdapter: SUCoinbaseStorageAdapter
) : LedgerStorageAdapter<StorageAwareCoinbase>,
    SchemaProvider by suCoinbaseStorageAdapter {
    override fun store(
        toStore: StorageAwareCoinbase, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suCoinbaseStorageAdapter,
            toStore, toStore.coinbase
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareCoinbase, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suCoinbaseStorageAdapter, ::StorageAwareCoinbase
        )
}