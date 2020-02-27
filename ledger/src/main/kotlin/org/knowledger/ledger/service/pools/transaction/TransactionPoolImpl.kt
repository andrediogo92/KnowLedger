package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.storage.Transaction

internal data class TransactionPoolImpl internal constructor(
    internal val adapterManager: AdapterManager,
    override val chainId: ChainId,
    internal val txs: MutableSet<PoolTransaction> = mutableSetOf()
) : TransactionPool {
    override val transactions: Set<PoolTransaction>
        get() = txs

    override fun invalidate(hash: Hash) {
        txs.removeIf {
            it.transaction.hash == hash
        }
    }

    override operator fun plusAssign(transaction: Transaction) {
        txs.add(PoolTransaction(adapterManager, transaction))
    }

    override operator fun minusAssign(transaction: Transaction) {
        txs.removeIf {
            it.transaction.hash == transaction.hash
        }
    }
}

