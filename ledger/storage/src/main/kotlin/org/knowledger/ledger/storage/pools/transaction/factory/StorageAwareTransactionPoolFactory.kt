package org.knowledger.ledger.storage.pools.transaction.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.TransactionPool
import org.knowledger.ledger.storage.pools.transaction.StorageAwareTransactionPool

internal class StorageAwareTransactionPoolFactory(
    private val transactionPoolFactory: TransactionPoolFactory,
) : TransactionPoolFactory {
    private fun createSA(pool: MutableTransactionPool): StorageAwareTransactionPool =
        StorageAwareTransactionPool(pool)

    override fun create(
        chainId: ChainId, txs: MutableSortedList<PoolTransaction>,
    ): StorageAwareTransactionPool =
        createSA(transactionPoolFactory.create(chainId, txs))

    override fun create(other: MutableTransactionPool): StorageAwareTransactionPool =
        createSA(transactionPoolFactory.create(other as TransactionPool))

    override fun create(pool: TransactionPool): StorageAwareTransactionPool =
        createSA(transactionPoolFactory.create(pool))
}