package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareBlockHeader(
    internal val blockHeader: StorageUnawareBlockHeader
) : BlockHeader by blockHeader,
    StorageAware<BlockHeader> {
    override fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidatedFields["merkleRoot"] = newRoot.bytes
            invalidatedFields["hash"] = hashId
            invalidatedFields["seconds"] = seconds
        }
    }

    override val invalidated: Map<String, Any>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private var invalidatedFields =
        mutableMapOf<String, Any>()

    internal constructor(
        chainId: ChainId, hasher: Hasher,
        previousHash: Hash, blockParams: BlockParams
    ) : this(
        StorageUnawareBlockHeader(
            chainId, hasher,
            previousHash, blockParams
        )
    )
}