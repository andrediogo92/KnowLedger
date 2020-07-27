package org.knowledger.ledger.storage.pools.transaction.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.TransactionPool
import org.knowledger.ledger.storage.pools.transaction.TransactionPoolImpl

internal class TransactionPoolFactoryImpl(
    private val poolTransactionFactory: PoolTransactionFactory
) : TransactionPoolFactory {
    override fun create(
        chainId: ChainId,
        txs: MutableSortedList<PoolTransaction>
    ): TransactionPoolImpl =
        TransactionPoolImpl(
            poolTransactionFactory = poolTransactionFactory,
            chainId = chainId, mutableTransactions = txs
        )

    override fun create(
        pool: TransactionPool
    ): TransactionPoolImpl = with(pool) {
        create(
            chainId = chainId,
            txs = transactions.toMutableSortedListFromPreSorted()
        )
    }

    override fun create(
        other: MutableTransactionPool
    ): TransactionPoolImpl =
        create(other as TransactionPool)
}