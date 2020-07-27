package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.MutableTransaction

interface MutableTransactionPool : TransactionPool {
    val mutableTransactions: MutableSortedList<PoolTransaction>
    fun invalidate(hash: Hash)
    operator fun plusAssign(transaction: MutableTransaction)
    operator fun minusAssign(transaction: MutableTransaction)
}