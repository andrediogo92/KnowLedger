package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import java.time.Instant

internal data class HashedBlockHeaderImpl(
    override val chainId: ChainId,
    override val params: BlockParams,
    private var _previousHash: Hash,
    private var _hash: Hash,
    private var _merkleRoot: Hash = Hash.emptyHash,
    private var _seconds: Long = Instant.now().epochSecond,
    private var _nonce: Long = Long.MIN_VALUE
) : MutableHashedBlockHeader {
    override val previousHash: Hash
        get() = _previousHash

    override val hash: Hash
        get() = _hash

    override val merkleRoot: Hash
        get() = _merkleRoot

    override val seconds: Long
        get() = _seconds

    override val nonce: Long
        get() = _nonce


    override fun updateHash(hash: Hash) {
        _hash = hash
    }

    override fun updateMerkleTree(newRoot: Hash) {
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

        if (chainId != other.chainId) return false
        if (previousHash != other.previousHash) return false
        if (params != other.params) return false
        if (_hash != other.hash) return false
        if (merkleRoot != other.merkleRoot) return false
        if (seconds != other.seconds) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + _hash.hashCode()
        result = 31 * result + merkleRoot.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }


}