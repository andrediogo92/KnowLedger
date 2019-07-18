package org.knowledger.ledger.service.pool

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageID
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.simpleUpdate

@JsonClass(generateAdapter = true)
data class StorageAwareTransactionPool internal constructor(
    internal val transactionPool: StorageUnawareTransactionPool,
    @Transient
    override var id: StorageID? = null
) : StorageAware<TransactionPool>,
    TransactionPool by transactionPool {
    override fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override val invalidated: Map<String, Any>
        get() = invalidatedFields

    internal constructor(
        chainId: ChainId
    ) : this(StorageUnawareTransactionPool(chainId))

    @Transient
    internal val invalidatedFields: MutableMap<String, Any> =
        mutableMapOf()
}