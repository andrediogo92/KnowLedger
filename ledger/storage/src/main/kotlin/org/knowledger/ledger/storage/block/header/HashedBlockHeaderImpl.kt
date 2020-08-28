package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockParams

internal data class HashedBlockHeaderImpl(
    override val chainHash: Hash,
    private var _hash: Hash,
    private var _merkleRoot: Hash,
    private var _previousHash: Hash,
    override val blockParams: BlockParams,
    private var _seconds: Long,
    private var _nonce: Long,
) : MutableHashedBlockHeader {
    override val previousHash: Hash get() = _previousHash

    override val hash: Hash get() = _hash

    override val merkleRoot: Hash get() = _merkleRoot

    override val seconds: Long get() = _seconds

    override val nonce: Long get() = _nonce


    override fun updateHash(hash: Hash) {
        _hash = hash
    }

    override fun updateMerkleRoot(merkleRoot: Hash) {
        _merkleRoot = merkleRoot
    }

    override fun nonceReset() {
        _nonce = Long.MIN_VALUE
    }

    override fun newNonce() {
        _nonce++
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedBlockHeader) return false

        if (chainHash != other.chainHash) return false
        if (_hash != other.hash) return false
        if (merkleRoot != other.merkleRoot) return false
        if (previousHash != other.previousHash) return false
        if (blockParams != other.blockParams) return false
        if (seconds != other.seconds) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainHash.hashCode()
        result = 31 * result + _hash.hashCode()
        result = 31 * result + merkleRoot.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + blockParams.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }


}