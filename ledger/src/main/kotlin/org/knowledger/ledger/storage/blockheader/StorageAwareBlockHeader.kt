package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.simpleUpdate

@Serializable
@SerialName("StorageBlockHeaderWrapper")
internal data class StorageAwareBlockHeader(
    internal val blockHeader: HashedBlockHeaderImpl
) : HashedBlockHeader,
    StorageAware<HashedBlockHeader> {
    override fun newHash() {
        blockHeader.newHash()
    }

    override fun clone(): HashedBlockHeader =
        blockHeader.clone()

    override val chainId: ChainId
        get() = blockHeader.chainId

    override val merkleRoot: Hash
        get() = blockHeader.merkleRoot

    override val previousHash: Hash
        get() = blockHeader.previousHash

    override val params: BlockParams
        get() = blockHeader.params

    override val seconds: Long
        get() = blockHeader.seconds

    override val nonce: Long
        get() = blockHeader.nonce

    override fun serialize(cbor: Cbor): ByteArray =
        blockHeader.serialize(cbor)

    override val hash: Hash
        get() = blockHeader.hash

    override fun update(
        session: NewInstanceSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidatedFields["merkleRoot"] = newRoot.bytes
            invalidatedFields["hash"] = hash
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
        chainId: ChainId, hasher: Hashers, cbor: Cbor,
        previousHash: Hash, blockParams: BlockParams
    ) : this(
        HashedBlockHeaderImpl(
            chainId, hasher, cbor,
            previousHash, blockParams
        )
    )
}