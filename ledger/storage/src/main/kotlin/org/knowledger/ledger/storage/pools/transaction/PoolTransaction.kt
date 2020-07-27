package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableTransaction

interface PoolTransaction : LedgerContract,
                            Comparable<PoolTransaction> {
    val transaction: MutableTransaction
    val inBlock: Boolean

    override fun compareTo(other: PoolTransaction): Int =
        transaction.compareTo(other.transaction)
}