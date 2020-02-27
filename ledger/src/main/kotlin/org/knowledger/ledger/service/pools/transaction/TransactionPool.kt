package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.storage.Transaction

internal interface TransactionPool : ServiceClass {
    val transactions: Set<PoolTransaction>
    val chainId: ChainId
    val unconfirmed: List<Transaction>
        get() = transactions.filter {
            !it.confirmed
        }.map {
            it.transaction
        }

    val firstUnconfirmed: Transaction?
        get() = unconfirmed.firstOrNull()

    fun invalidate(hash: Hash)

    fun unconfirmed(hash: Hash): Boolean =
        unconfirmed.any {
            it.hash == hash
        }

    fun confirmed(hash: Hash): Boolean =
        transactions.firstOrNull {
            it.transaction.hash == hash
        }?.confirmed ?: false

    operator fun get(transaction: Transaction): PoolTransaction? =
        get(transaction.hash)

    operator fun get(hash: Hash): PoolTransaction? =
        transactions.firstOrNull {
            it.transaction.hash == hash
        }

    operator fun plusAssign(transaction: Transaction)

    operator fun minusAssign(transaction: Transaction)
}