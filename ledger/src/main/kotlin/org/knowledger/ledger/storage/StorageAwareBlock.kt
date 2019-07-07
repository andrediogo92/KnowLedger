package org.knowledger.ledger.storage

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.StorageID

@JsonClass(generateAdapter = true)
data class StorageAwareBlock(
    internal val block: StorageUnawareBlock
) : Block by block, StorageAware<Block> {
    @Transient
    override var id: StorageID? = null
}