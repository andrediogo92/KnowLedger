package org.knowledger.ledger.storage.config.chainid

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageAwareChainIdImpl(
    override val chainId: ChainId,
) : ChainId by chainId, StorageAwareChainId {
    override val lock: Locking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()

    override fun equals(other: Any?): Boolean = chainId == other

    override fun hashCode(): Int = chainId.hashCode()
}