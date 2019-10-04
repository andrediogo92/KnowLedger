package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.addOrReplaceInstances
import org.knowledger.ledger.storage.simpleUpdate

@Serializable
@SerialName("StorageBlockHeaderWrapper")
internal data class StorageAwareBlockHeader(
    internal val blockHeader: HashedBlockHeaderImpl
) : HashedBlockHeader by blockHeader,
    StorageAware<HashedBlockHeader> {
    override val hash: Hash
        get() = blockHeader.hash

    override val invalidated: List<StoragePairs>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private var invalidatedFields =
        mutableListOf<StoragePairs>()


    override fun newHash() {
        blockHeader.newHash()
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidatedFields.addOrReplaceInstances(
                arrayOf("merkleRoot", "hash", "seconds"),
                arrayOf(
                    StoragePairs.Element.Hash(newRoot),
                    StoragePairs.Element.Hash(hash),
                    StoragePairs.Element.Native(seconds)
                )
            )
        }
    }

    override fun clone(): HashedBlockHeader =
        blockHeader.clone()


    override fun serialize(encoder: BinaryFormat): ByteArray =
        blockHeader.serialize(encoder)



    internal constructor(
        chainId: ChainId, hasher: Hashers, encoder: BinaryFormat,
        previousHash: Hash, blockParams: BlockParams
    ) : this(
        blockHeader = HashedBlockHeaderImpl(
            chainId = chainId, hasher = hasher,
            encoder = encoder, previousHash = previousHash,
            blockParams = blockParams
        )
    )
}