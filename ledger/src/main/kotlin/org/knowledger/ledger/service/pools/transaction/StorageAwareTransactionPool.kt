package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareTransactionPool internal constructor(
    internal val transactionPool: TransactionPoolImpl
) : StorageAware<TransactionPool>,
    TransactionPool by transactionPool {
    override var id: StorageID? = null

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.LinkedSet(
                "transactions", TransactionStorageAdapter
            )
        )

    internal constructor(
        chainId: ChainId
    ) : this(TransactionPoolImpl(chainId))

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