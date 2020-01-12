package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SATransactionOutputStorageAdapter(
    private val suTransactionStorageAdapter: SUTransactionOutputStorageAdapter
) : LedgerStorageAdapter<StorageAwareTransactionOutput>,
    SchemaProvider by suTransactionStorageAdapter {
    override fun store(
        toStore: StorageAwareTransactionOutput,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suTransactionStorageAdapter, toStore,
            toStore.transactionOutput
        )

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareTransactionOutput, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionStorageAdapter,
            ::StorageAwareTransactionOutput
        )
}