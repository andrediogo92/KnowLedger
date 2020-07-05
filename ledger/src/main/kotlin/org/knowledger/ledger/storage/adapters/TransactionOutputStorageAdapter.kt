package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.SUTransactionOutputStorageAdapter
import org.knowledger.ledger.storage.transaction.output.StorageAwareTransactionOutput
import org.knowledger.ledger.storage.transaction.output.factory.StorageAwareTransactionOutputFactory

internal class TransactionOutputStorageAdapter(
    transactionOutputFactory: StorageAwareTransactionOutputFactory
) : LedgerStorageAdapter<TransactionOutput> {
    private val suTransactionOutputStorageAdapter = SUTransactionOutputStorageAdapter(
        transactionOutputFactory
    )

    override val id: String
        get() = suTransactionOutputStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suTransactionOutputStorageAdapter.properties

    override fun store(
        toStore: TransactionOutput,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransactionOutput -> session.cacheStore(
                suTransactionOutputStorageAdapter, toStore,
                toStore.transactionOutput
            )
            else -> suTransactionOutputStorageAdapter.store(toStore, session)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareTransactionOutput, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionOutputStorageAdapter
        )
}