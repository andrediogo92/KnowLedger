package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.SATransactionOutputStorageAdapter
import org.knowledger.ledger.storage.transaction.output.SUTransactionOutputStorageAdapter
import org.knowledger.ledger.storage.transaction.output.StorageAwareTransactionOutput
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl

internal class TransactionOutputStorageAdapter(
    private val suTransactionOutputStorageAdapter: SUTransactionOutputStorageAdapter,
    private val saTransactionOutputStorageAdapter: SATransactionOutputStorageAdapter
) : LedgerStorageAdapter<TransactionOutput>,
    SchemaProvider by suTransactionOutputStorageAdapter {
    override fun store(
        toStore: TransactionOutput,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransactionOutput ->
                saTransactionOutputStorageAdapter.store(toStore, session)
            is TransactionOutputImpl ->
                suTransactionOutputStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionOutput, LoadFailure> =
        saTransactionOutputStorageAdapter.load(ledgerHash, element)

}