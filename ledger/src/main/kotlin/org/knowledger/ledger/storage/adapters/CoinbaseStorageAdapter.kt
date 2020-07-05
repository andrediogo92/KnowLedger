package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.coinbase.SUCoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.coinbase.factory.StorageAwareCoinbaseFactory

internal class CoinbaseStorageAdapter(
    coinbaseFactory: StorageAwareCoinbaseFactory,
    merkleTreeStorageAdapter: MerkleTreeStorageAdapter,
    coinbaseHeaderStorageAdapter: CoinbaseHeaderStorageAdapter,
    witnessStorageAdapter: WitnessStorageAdapter
) : LedgerStorageAdapter<MutableCoinbase> {
    private val suCoinbaseStorageAdapter =
        SUCoinbaseStorageAdapter(
            coinbaseFactory, merkleTreeStorageAdapter,
            coinbaseHeaderStorageAdapter,
            witnessStorageAdapter
        )

    override val id: String
        get() = suCoinbaseStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suCoinbaseStorageAdapter.properties


    override fun store(
        toStore: MutableCoinbase, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareCoinbase ->
                session.cacheStore(
                    suCoinbaseStorageAdapter,
                    toStore, toStore.coinbase
                )
            else ->
                suCoinbaseStorageAdapter.store(toStore, session)
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareCoinbase, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suCoinbaseStorageAdapter
        )

}