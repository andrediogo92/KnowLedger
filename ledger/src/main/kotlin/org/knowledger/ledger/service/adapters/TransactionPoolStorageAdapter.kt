package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.pools.transaction.SATransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.SUTransactionPoolStorageAdapter
import org.knowledger.ledger.service.pools.transaction.StorageAwareTransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.pools.transaction.TransactionPoolImpl
import org.knowledger.ledger.service.results.LedgerFailure

internal class TransactionPoolStorageAdapter(
    private val suTransactionPoolStorageAdapter: SUTransactionPoolStorageAdapter,
    private val saTransactionPoolStorageAdapter: SATransactionPoolStorageAdapter
) : ServiceStorageAdapter<TransactionPool>,
    SchemaProvider by suTransactionPoolStorageAdapter {
    override fun store(
        toStore: TransactionPool,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareTransactionPool ->
                saTransactionPoolStorageAdapter.store(toStore, session)
            is TransactionPoolImpl ->
                suTransactionPoolStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<TransactionPool, LedgerFailure> =
        saTransactionPoolStorageAdapter.load(ledgerHash, element)
}