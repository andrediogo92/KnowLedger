package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.storage.HashUpdateable

@Serializable
@SerialName("HashedBlockHeader")
internal data class HashedBlockHeaderImpl(
    internal val blockHeader: BlockHeaderImpl,
    @SerialName("hash")
    internal var _hash: Hash? = null,
    @Transient
    private var hasher: Hashers = DEFAULT_HASHER,
    @Transient
    private var encoder: BinaryFormat = Cbor.plain
) : HashedBlockHeader,
    HashUpdateable,
    BlockHeader by blockHeader {
    @Transient
    private var cachedSize: Long? = null

    override val approximateSize: Long
        get() = cachedSize ?: recalculateSize(hasher, encoder)

    override val hash: Hash
        get() = _hash ?: recalculateHash(hasher, encoder)


    internal constructor(
        chainId: ChainId, hasher: Hashers, encoder: BinaryFormat,
        previousHash: Hash, blockParams: BlockParams
    ) : this(
        blockHeader = BlockHeaderImpl(
            chainId = chainId,
            previousHash = previousHash,
            params = blockParams
        ),
        hasher = hasher,
        encoder = encoder
    )

    internal constructor(
        chainId: ChainId, hasher: Hashers,
        encoder: BinaryFormat, hash: Hash,
        blockParams: BlockParams, previousHash: Hash,
        merkleRoot: Hash, seconds: Long, nonce: Long
    ) : this(
        blockHeader = BlockHeaderImpl(
            chainId = chainId,
            previousHash = previousHash,
            params = blockParams, _merkleRoot = merkleRoot,
            seconds = seconds, _nonce = nonce
        ), _hash = hash, hasher = hasher, encoder = encoder
    )

    override fun serialize(encoder: BinaryFormat): ByteArray =
        blockHeader.serialize(encoder)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader._merkleRoot = newRoot
        blockHeader._nonce = 0
        updateHash(hasher, encoder)
    }

    override fun recalculateSize(
        hasher: Hasher, encoder: BinaryFormat
    ): Long {
        updateHash(hasher, encoder)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hasher, encoder: BinaryFormat
    ): Hash {
        updateHash(hasher, encoder)
        return _hash as Hash
    }

    override fun updateHash(
        hasher: Hasher, encoder: BinaryFormat
    ) {
        val bytes = blockHeader.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun newHash() {
        blockHeader._nonce++
        updateHash(hasher, encoder)
    }


    override fun clone(): HashedBlockHeader =
        copy()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedBlockHeaderImpl) return false

        if (blockHeader != other.blockHeader) return false
        if (_hash != other._hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockHeader.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}