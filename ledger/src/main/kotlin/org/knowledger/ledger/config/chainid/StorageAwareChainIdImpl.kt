package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs

internal data class StorageAwareChainIdImpl(
    override val chainId: ChainId
) : ChainId by chainId, StorageAwareChainId {
    override val invalidated: Array<StoragePairs<*>>
        get() = emptyArray()

    override var id: StorageID? = null

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        Outcome.Ok(id!!)

    override fun equals(other: Any?): Boolean =
        chainId == other

    override fun hashCode(): Int =
        chainId.hashCode()
}