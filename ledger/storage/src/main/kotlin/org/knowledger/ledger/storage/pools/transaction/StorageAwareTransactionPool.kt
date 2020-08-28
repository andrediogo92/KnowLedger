package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.cache.StorageAware
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal data class StorageAwareTransactionPool(
    internal val transactionPool: TransactionPoolImpl,
) : StorageAware, MutableTransactionPool by transactionPool {
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.LinkedList<PoolTransaction>("transactions", AdapterIds.PoolTransaction)
    )

    override var id: StorageElement? = null

    override fun invalidate(hash: Hash) {
        transactionPool.invalidate(hash)
        if (id != null) {
            invalidated.replaceUnchecked(0, mutableTransactions)
        }
    }

    override fun minusAssign(transaction: MutableTransaction) {
        transactionPool -= transaction
        if (id != null) {
            invalidated.replaceUnchecked(0, mutableTransactions)
        }
    }

    override fun plusAssign(transaction: MutableTransaction) {
        transactionPool += transaction
        if (id != null) {
            invalidated.replaceUnchecked(0, mutableTransactions)
        }
    }


}