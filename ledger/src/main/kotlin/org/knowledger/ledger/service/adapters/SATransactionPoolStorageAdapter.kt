package org.knowledger.ledger.service.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.service.pool.StorageAwareTransactionPool
import org.knowledger.ledger.service.results.LedgerFailure

object SATransactionPoolStorageAdapter : ServiceStorageAdapter<StorageAwareTransactionPool> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: StorageAwareTransactionPool,
        session: NewInstanceSession
    ): StorageElement =
        toStore.id?.element ?: SUTransactionPoolStorageAdapter
            .store(toStore.transactionPool, session)
            .also {
                toStore.id = it.identity
            }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareTransactionPool, LedgerFailure> =
        SUTransactionPoolStorageAdapter
            .load(ledgerHash, element)
            .mapSuccess {
                StorageAwareTransactionPool(it, element.identity)
            }
}