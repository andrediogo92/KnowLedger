package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SATransactionStorageAdapter(
    private val suTransactionAdapter: SUTransactionStorageAdapter
) : LedgerStorageAdapter<StorageAwareTransaction>,
    SchemaProvider by suTransactionAdapter {
    override fun store(
        toStore: StorageAwareTransaction,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(suTransactionAdapter, toStore, toStore.transaction)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareTransaction, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionAdapter, ::StorageAwareTransaction
        )
}