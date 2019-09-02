package org.knowledger.ledger.config.chainid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware

@Serializable
@SerialName("StorageChainIdWrapper")
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
        tag: Tag, ledgerHash: Hash,
        hash: Hash
    ) : this(StorageUnawareChainId(tag, ledgerHash, hash))

    constructor(
        tag: Tag, ledgerHash: Hash,
        hasher: Hasher, cbor: Cbor
    ) : this(StorageUnawareChainId(hasher, cbor, tag, ledgerHash))
}