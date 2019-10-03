package org.knowledger.ledger.service.pools.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.simpleUpdate

@Serializable
@SerialName("StorageTransactionPoolWrapper")
data class StorageAwareTransactionPool internal constructor(
    internal val transactionPool: TransactionPoolImpl,
    @Transient
    override var id: StorageID? = null
) : StorageAware<TransactionPool>,
    TransactionPool by transactionPool {
    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override val invalidated: List<StoragePairs>
        get() = invalidatedFields

    internal constructor(
        chainId: ChainId
    ) : this(TransactionPoolImpl(chainId))

    @Transient
    internal val invalidatedFields: MutableList<StoragePairs> =
        mutableListOf()
}