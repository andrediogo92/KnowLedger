package org.knowledger.ledger.storage.pools.transaction.factory

import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.pools.transaction.PoolTransaction
import org.knowledger.ledger.storage.pools.transaction.PoolTransactionImpl

internal class PoolTransactionFactoryImpl : PoolTransactionFactory {
    override fun create(transaction: MutableTransaction, inBlock: Boolean): PoolTransactionImpl =
        PoolTransactionImpl(transaction, inBlock)

    override fun create(transaction: MutableTransaction): PoolTransactionImpl =
        create(transaction = transaction, inBlock = false)

    override fun create(other: PoolTransaction): PoolTransactionImpl =
        with(other) { create(transaction, inBlock) }
}