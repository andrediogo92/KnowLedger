package org.knowledger.ledger.storage.coinbase

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SACoinbaseStorageAdapter : LedgerStorageAdapter<StorageAwareCoinbase> {
    override val id: String
        get() = CoinbaseStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = CoinbaseStorageAdapter.properties

    override fun store(
        toStore: StorageAwareCoinbase, session: NewInstanceSession
    ): StorageElement =
        session.cacheStore(
            SUCoinbaseStorageAdapter,
            toStore, toStore.coinbase
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareCoinbase, LoadFailure> =
        element.cachedLoad(ledgerHash, SUCoinbaseStorageAdapter) {
            StorageAwareCoinbase(it)
        }
}