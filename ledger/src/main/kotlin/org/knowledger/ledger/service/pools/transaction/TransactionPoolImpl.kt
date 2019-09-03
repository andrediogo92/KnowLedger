package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash

@Serializable
@SerialName("TransactionPool")
data class TransactionPoolImpl internal constructor(
    internal val chainId: ChainId,
    internal val txs: MutableList<Hash> = mutableListOf(),
    internal val confirm: MutableList<Boolean> = mutableListOf()
) : TransactionPool {
    override val transactions: List<Hash>
        get() = txs

    override val confirmations: List<Boolean>
        get() = confirm


    operator fun plus(transaction: Hash): Boolean {
        val first = txs.add(transaction)
        return if (first) {
            val second = confirm.add(true)
            if (second) {
                second
            } else {
                txs.remove(transaction)
                second
            }
        } else {
            first
        }
    }

    operator fun minus(transaction: Hash): Boolean {
        val index = txs.indexOf(transaction)
        return if (index != -1) {
            txs.removeAt(index)
            confirm.removeAt(index)
            true
        } else {
            false
        }
    }
}