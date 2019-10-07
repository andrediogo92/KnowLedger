package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Hashers
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareBlockHeader(
    internal val blockHeader: HashedBlockHeaderImpl
) : HashedBlockHeader by blockHeader,
    StorageAware<HashedBlockHeader> {
    override val hash: Hash
        get() = blockHeader.hash

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Hash("merkleRoot"),
            StoragePairs.Hash("hash"),
            StoragePairs.Native("seconds")
        )

    override var id: StorageID? = null

    internal constructor(
        chainId: ChainId, hasher: Hashers,
        encoder: BinaryFormat, previousHash: Hash,
        blockParams: BlockParams
    ) : this(
        blockHeader = HashedBlockHeaderImpl(
            chainId = chainId, hasher = hasher,
            encoder = encoder, previousHash = previousHash,
            blockParams = blockParams
        )
    )

    override fun newHash() {
        blockHeader.newHash()
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidated.replace(0, newRoot)
            invalidated.replace(1, hash)
            invalidated.replace(2, seconds)
        }
    }
}