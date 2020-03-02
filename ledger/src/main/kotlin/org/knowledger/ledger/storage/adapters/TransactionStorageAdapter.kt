package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.SATransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.SUTransactionStorageAdapter
import org.knowledger.ledger.storage.transaction.StorageAwareTransaction

internal class TransactionStorageAdapter(
    private val suTransactionStorageAdapter: SUTransactionStorageAdapter,
    private val saTransactionStorageAdapter: SATransactionStorageAdapter
) : LedgerStorageAdapter<Transaction>,
    SchemaProvider by suTransactionStorageAdapter {
    override fun store(
        toStore: Transaction,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransaction ->
                saTransactionStorageAdapter.store(toStore, session)
            is HashedTransactionImpl ->
                suTransactionStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Transaction, LoadFailure> =
        saTransactionStorageAdapter.load(ledgerHash, element)

}