package org.knowledger.ledger.config.chainid

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs

internal data class StorageAwareChainId(
    internal val chainId: ChainIdImpl
) : ChainId by chainId, StorageAware<ChainId> {
    override val invalidated: Array<StoragePairs<*>>
        get() = emptyArray()

    override var id: StorageID? = null

    internal constructor(
        tag: Tag, ledgerHash: Hash, hash: Hash
    ) : this(
        ChainIdImpl(
            tag, ledgerHash, hash
        )
    )

    internal constructor(
        tag: Tag, ledgerHash: Hash,
        hasher: Hasher, encoder: BinaryFormat
    ) : this(
        ChainIdImpl(
            hasher, encoder,
            tag, ledgerHash
        )
    )

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        Outcome.Ok(id!!)

    override fun equals(other: Any?): Boolean =
        chainId == other

    override fun hashCode(): Int =
        chainId.hashCode()
}