package org.knowledger.ledger.service.pool

import com.squareup.moshi.JsonClass
import org.knowledger.common.hash.Hash
import org.knowledger.ledger.config.ChainId

@JsonClass(generateAdapter = true)
data class StorageUnawareTransactionPool internal constructor(
    internal val chainId: ChainId,
    internal val txs: MutableList<Hash> = mutableListOf(),
    internal val confirm: MutableList<Boolean> = mutableListOf()
) : TransactionPool {
    override val transactions: List<Hash>
        get() = txs

    override val confirmations: List<Boolean>
        get() = confirm

    override val unconfirmed: List<Hash>
        get() = transactions.filterIndexed { index, _ ->
            !confirm[index]
        }

    override val firstUnconfirmed: Hash?
        get() = transactions.asSequence().filterIndexed { index, _ ->
            !confirm[index]
        }.first()

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