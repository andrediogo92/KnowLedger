package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.NonceRegen
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.simpleUpdate

internal class StorageAwareBlockHeader(
    internal val blockHeader: HashedBlockHeaderImpl
) : HashedBlockHeader by blockHeader,
    MerkleTreeUpdate, NonceRegen, HashRegen,
    StorageAware<HashedBlockHeader> {
    override val hash: Hash
        get() = blockHeader.hash

    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Native("nonce"),
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

    override fun updateHash() {
        blockHeader.updateHash()
        invalidated.replace(2, hash)
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidated.replace(1, newRoot)
            invalidated.replace(2, hash)
            invalidated.replace(3, seconds)
        }
    }

    override fun newNonce() {
        blockHeader.newNonce()
        invalidated.replace(0, nonce)
        invalidated.replace(2, hash)
    }

    override fun equals(other: Any?): Boolean =
        blockHeader == other

    override fun hashCode(): Int =
        blockHeader.hashCode()
}