package org.knowledger.ledger.storage.pools.transaction.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.TransactionPool

interface TransactionPoolFactory : CloningFactory<MutableTransactionPool> {
    fun create(
        chainId: ChainId,
        txs: MutableSortedList<PoolTransaction>
    ): MutableTransactionPool

    fun create(
        pool: TransactionPool
    ): MutableTransactionPool
}