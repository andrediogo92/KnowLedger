package org.knowledger.ledger.storage.pools.transaction.factory

import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.pools.transaction.PoolTransaction

interface PoolTransactionFactory : CloningFactory<PoolTransaction> {
    fun create(
        transaction: MutableTransaction, inBlock: Boolean
    ): PoolTransaction

    fun create(
        transaction: MutableTransaction
    ): PoolTransaction
}