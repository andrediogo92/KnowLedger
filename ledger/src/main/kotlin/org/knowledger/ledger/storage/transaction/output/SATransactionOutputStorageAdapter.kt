package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal object SATransactionOutputStorageAdapter : LedgerStorageAdapter<StorageAwareTransactionOutput> {
    override val id: String
        get() = TransactionOutputStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionOutputStorageAdapter.properties

    override fun store(
        toStore: StorageAwareTransactionOutput, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUTransactionOutputStorageAdapter, toStore, toStore.transactionOutput
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareTransactionOutput, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUTransactionOutputStorageAdapter, ::StorageAwareTransactionOutput
        )
}