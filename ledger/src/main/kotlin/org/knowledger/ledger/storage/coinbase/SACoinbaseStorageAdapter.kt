package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SACoinbaseStorageAdapter : LedgerStorageAdapter<StorageAwareCoinbase> {
    override val id: String
        get() = CoinbaseStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = CoinbaseStorageAdapter.properties

    override fun store(
        toStore: StorageAwareCoinbase, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUCoinbaseStorageAdapter,
            toStore, toStore.coinbase
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareCoinbase, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUCoinbaseStorageAdapter, ::StorageAwareCoinbase
        )
}