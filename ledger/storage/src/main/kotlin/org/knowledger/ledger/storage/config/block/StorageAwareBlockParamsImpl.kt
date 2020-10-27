package org.knowledger.ledger.storage.config.block

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.StoragePairs

internal data class StorageAwareBlockParamsImpl(
    override val blockParams: BlockParams,
) : BlockParams by blockParams, StorageAwareBlockParams {
    override val lock: BooleanLocking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()

    override fun equals(other: Any?): Boolean = blockParams == other

    override fun hashCode(): Int = blockParams.hashCode()
}