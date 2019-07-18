package org.knowledger.ledger.config.chainid

import com.squareup.moshi.JsonClass
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageID
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware

@JsonClass(generateAdapter = true)
data class StorageAwareChainId(
    val chainId: StorageUnawareChainId
) : ChainId by chainId, StorageAware<ChainId> {
    override fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure> =
        Outcome.Ok(id!!)

    override val invalidated: Map<String, Any>
        get() = emptyMap()

    @Transient
    override var id: StorageID? = null

    constructor(
        tag: String, ledgerHash: Hash, hash: Hash
    ) : this(StorageUnawareChainId(tag, ledgerHash, hash))

    constructor(
        tag: String, ledgerHash: Hash, hasher: Hasher
    ) : this(StorageUnawareChainId(tag, ledgerHash, hasher))
}