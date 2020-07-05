package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.transaction.SUTransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction
import org.knowledger.ledger.storage.transaction.factory.StorageAwareTransactionFactory

internal class TransactionStorageAdapter(
    ledgerInfo: LedgerInfo,
    physicalDataStorageAdapter: PhysicalDataStorageAdapter,
    saTransactionFactory: StorageAwareTransactionFactory
) : LedgerStorageAdapter<MutableTransaction> {
    private val suTransactionStorageAdapter: SUTransactionStorageAdapter =
        SUTransactionStorageAdapter(
            ledgerInfo = ledgerInfo,
            physicalDataStorageAdapter = physicalDataStorageAdapter,
            transactionFactory = saTransactionFactory
        )

    override val id: String
        get() = suTransactionStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suTransactionStorageAdapter.properties

    override fun store(
        toStore: MutableTransaction,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransaction -> session.cacheStore(
                suTransactionStorageAdapter, toStore,
                toStore.transaction
            )
            else ->
                suTransactionStorageAdapter.store(toStore, session)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareTransaction, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suTransactionStorageAdapter
        )

}