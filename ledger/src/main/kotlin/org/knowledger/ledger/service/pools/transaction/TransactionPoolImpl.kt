package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.misc.removeByUnique

@Serializable
@SerialName("TransactionPool")
data class TransactionPoolImpl internal constructor(
    internal val chainId: ChainId,
    internal val txs: MutableSet<PoolTransaction> = mutableSetOf()
) : TransactionPool {
    override val transactions: Set<PoolTransaction>
        get() = txs

    operator fun plus(transaction: StorageID): Boolean =
        txs.add(PoolTransaction(transaction))

    operator fun minus(transaction: StorageID): Boolean =
        txs.removeByUnique {
            it.id == transaction
        }
}

