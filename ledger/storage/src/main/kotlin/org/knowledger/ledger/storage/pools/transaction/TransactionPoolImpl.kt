package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactory

internal class TransactionPoolImpl(
    private val poolTransactionFactory: PoolTransactionFactory,
    override val chainId: ChainId,
    override val mutableTransactions: MutableSortedList<PoolTransaction>,
) : MutableTransactionPool {
    override val transactions: SortedList<PoolTransaction>
        get() = mutableTransactions

    override fun invalidate(hash: Hash) {
        mutableTransactions.removeIf { it.transaction.hash == hash }
    }

    override operator fun plusAssign(transaction: MutableTransaction) {
        mutableTransactions.add(poolTransactionFactory.create(transaction))
    }

    override operator fun minusAssign(transaction: MutableTransaction) {
        mutableTransactions.removeIf { it.transaction.hash == transaction.hash }
    }
}

