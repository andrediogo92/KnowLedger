package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.SUCoinbaseHeaderStorageAdapter
import org.knowledger.ledger.storage.coinbase.header.StorageAwareCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.factory.StorageAwareCoinbaseHeaderFactory

internal class CoinbaseHeaderStorageAdapter(
    saCoinbaseHeaderFactory: StorageAwareCoinbaseHeaderFactory
) : LedgerStorageAdapter<MutableCoinbaseHeader> {
    private val suCoinbaseHeaderStorageAdapter =
        SUCoinbaseHeaderStorageAdapter(saCoinbaseHeaderFactory)

    override val id: String
        get() = suCoinbaseHeaderStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suCoinbaseHeaderStorageAdapter.properties

    override fun store(
        toStore: MutableCoinbaseHeader, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareCoinbaseHeader -> session.cacheStore(
                suCoinbaseHeaderStorageAdapter,
                toStore, toStore.coinbaseHeader
            )
            else -> suCoinbaseHeaderStorageAdapter.store(toStore, session)
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareCoinbaseHeader, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suCoinbaseHeaderStorageAdapter
        )
}