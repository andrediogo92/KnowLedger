package org.knowledger.ledger.storage.blockheader

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.NonceRegen

internal data class HashedBlockHeaderImpl(
    internal val blockHeader: BlockHeaderImpl,
    internal var _hash: Hash? = null,
    private var hasher: Hashers = DEFAULT_HASHER,
    private var encoder: BinaryFormat = Cbor
) : HashedBlockHeader, HashUpdateable,
    MerkleTreeUpdate, HashRegen, NonceRegen,
    BlockHeader by blockHeader {
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

    override fun recalculateSize(
        hasher: Hashers, encoder: BinaryFormat
    ): Long {
        updateHash(hasher, encoder)
        return cachedSize as Long
    }

    override fun recalculateHash(
        hasher: Hashers, encoder: BinaryFormat
    ): Hash {
        updateHash(hasher, encoder)
        return _hash as Hash
    }

    override fun updateHash(
        hasher: Hashers, encoder: BinaryFormat
    ) {
        val bytes = blockHeader.serialize(encoder)
        _hash = hasher.applyHash(bytes)
        cachedSize = cachedSize ?: bytes.size.toLong() +
                _hash!!.bytes.size.toLong()
    }

    override fun updateMerkleTree(newRoot: Hash) {
        (blockHeader as MerkleTreeUpdate).updateMerkleTree(newRoot)
        (blockHeader as NonceReset).nonceReset()
        updateHash(hasher, encoder)
    }

    override fun updateHash() {
        updateHash(hasher, encoder)
    }

    override fun newNonce() {
        blockHeader.newNonce()
        updateHash()
    }


    override fun clone(): HashedBlockHeader =
        copy(
            blockHeader = blockHeader.clone()
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedBlockHeader) return false

        if (blockHeader != other) return false
        if (_hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockHeader.hashCode()
        result = 31 * result + (_hash?.hashCode() ?: 0)
        return result
    }


}