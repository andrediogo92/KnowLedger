package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareTransactionPool internal constructor(
    internal val adapterManager: AdapterManager,
    internal val transactionPool: TransactionPoolImpl
) : StorageAware<TransactionPool>,
    TransactionPool by transactionPool {
    override var id: StorageID? = null

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.LinkedSet(
                "transactions", adapterManager.transactionStorageAdapter
            )
        )

    internal constructor(
        adapterManager: AdapterManager,
        chainId: ChainId
    ) : this(adapterManager, TransactionPoolImpl(adapterManager, chainId))

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun invalidate(hash: Hash) {
        transactionPool.invalidate(hash)
        invalidated.replace(0, transactions)
    }

    override fun minusAssign(transaction: Transaction) {
        transactionPool -= transaction
        invalidated.replace(0, transactions)
    }

    override fun plusAssign(transaction: Transaction) {
        transactionPool += transaction
        invalidated.replace(0, transactions)
    }


}