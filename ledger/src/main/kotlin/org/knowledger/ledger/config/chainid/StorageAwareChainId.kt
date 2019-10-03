package org.knowledger.ledger.config.chainid

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs

@Serializable
@SerialName("StorageChainIdWrapper")
data class StorageAwareChainId(
    val chainId: StorageUnawareChainId
) : ChainId by chainId, StorageAware<ChainId> {
    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        Outcome.Ok(id!!)

    override val invalidated: List<StoragePairs>
        get() = emptyList()

    @Transient
    override var id: StorageID? = null

    constructor(
        tag: Tag, ledgerHash: Hash,
        hash: Hash
    ) : this(StorageUnawareChainId(tag, ledgerHash, hash))

    constructor(
        tag: Tag, ledgerHash: Hash,
        hasher: Hasher, encoder: BinaryFormat
    ) : this(StorageUnawareChainId(hasher, encoder, tag, ledgerHash))
}