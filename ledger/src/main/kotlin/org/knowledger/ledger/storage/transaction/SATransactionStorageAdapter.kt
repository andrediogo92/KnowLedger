package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter

internal object SATransactionStorageAdapter : LedgerStorageAdapter<StorageAwareTransaction> {
    override val id: String
        get() = TransactionStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionStorageAdapter.properties

    override fun store(toStore: StorageAwareTransaction, session: ManagedSession): StorageElement =
        session.cacheStore(SUTransactionStorageAdapter, toStore, toStore.transaction)

    override fun load(ledgerHash: Hash, element: StorageElement): Outcome<StorageAwareTransaction, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUTransactionStorageAdapter, ::StorageAwareTransaction
        )
}