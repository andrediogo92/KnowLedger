package org.knowledger.ledger.config

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.StorageID
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.ledger.storage.StorageAware

@JsonClass(generateAdapter = true)
data class StorageAwareChainId(
    val chainId: StorageUnawareChainId
) : ChainId by chainId, StorageAware<ChainId> {
    @Transient
    override var id: StorageID? = null

    constructor(
        tag: String, ledgerHash: Hash, hash: Hash
    ) : this(StorageUnawareChainId(tag, ledgerHash, hash))

    constructor(
        tag: String, ledgerHash: Hash, hasher: Hasher
    ) : this(StorageUnawareChainId(tag, ledgerHash, hasher))
}