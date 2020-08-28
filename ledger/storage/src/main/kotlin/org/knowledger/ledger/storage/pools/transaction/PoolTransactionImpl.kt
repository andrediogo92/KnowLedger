package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.cache.StoragePairs

internal class PoolTransactionImpl(
    override val transaction: MutableTransaction,
    override val inBlock: Boolean,
) : StorageAwarePoolTransaction {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.Linked<MutableTransaction>("transaction", AdapterIds.Transaction),
        StoragePairs.Native("confirmed")
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PoolTransaction) return false


        if (transaction != other.transaction) return false
        if (inBlock != other.inBlock) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transaction.hashCode()
        result = 31 * result + inBlock.hashCode()
        return result
    }

}