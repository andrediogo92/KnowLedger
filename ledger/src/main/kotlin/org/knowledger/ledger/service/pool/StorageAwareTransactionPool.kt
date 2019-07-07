package org.knowledger.ledger.service.pool

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.StorageID
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.storage.StorageAware

@JsonClass(generateAdapter = true)
data class StorageAwareTransactionPool internal constructor(
    internal val transactionPool: StorageUnawareTransactionPool,
    @Transient
    override var id: StorageID? = null
) : StorageAware<TransactionPool>,
    TransactionPool by transactionPool {
    internal constructor(
        chainId: ChainId
    ) : this(StorageUnawareTransactionPool(chainId))
}