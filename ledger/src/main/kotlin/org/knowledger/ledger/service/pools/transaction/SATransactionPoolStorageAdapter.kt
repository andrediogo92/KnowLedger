package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal class SATransactionPoolStorageAdapter(
    private val adapterManager: AdapterManager,
    private val suTransactionPoolStorageAdapter: SUTransactionPoolStorageAdapter
) : ServiceStorageAdapter<StorageAwareTransactionPool>,
    SchemaProvider by suTransactionPoolStorageAdapter {
    override fun store(
        toStore: StorageAwareTransactionPool,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suTransactionPoolStorageAdapter,
            toStore, toStore.transactionPool
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareTransactionPool, LedgerFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionPoolStorageAdapter
        ) {
            StorageAwareTransactionPool(
                adapterManager,
                it
            )
        }
}