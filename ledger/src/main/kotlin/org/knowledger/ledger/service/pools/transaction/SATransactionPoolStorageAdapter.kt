package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object SATransactionPoolStorageAdapter :
    ServiceStorageAdapter<StorageAwareTransactionPool> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: StorageAwareTransactionPool,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUTransactionPoolStorageAdapter,
            toStore, toStore.transactionPool
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareTransactionPool, LedgerFailure> =
        element.cachedLoad(ledgerHash, SUTransactionPoolStorageAdapter) {
            StorageAwareTransactionPool(it)
        }
}