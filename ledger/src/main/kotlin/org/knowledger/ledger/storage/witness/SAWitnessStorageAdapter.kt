package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SAWitnessStorageAdapter(
    private val suTransactionStorageAdapter: SUWitnessStorageAdapter
) : LedgerStorageAdapter<StorageAwareWitness>,
    SchemaProvider by suTransactionStorageAdapter {
    override fun store(
        toStore: StorageAwareWitness,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suTransactionStorageAdapter, toStore,
            toStore.transactionOutput
        )

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareWitness, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionStorageAdapter,
            ::StorageAwareWitness
        )
}