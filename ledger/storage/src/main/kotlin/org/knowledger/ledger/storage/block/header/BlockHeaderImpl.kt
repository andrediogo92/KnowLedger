package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockParams

internal data class BlockHeaderImpl(
    override val chainHash: Hash,
    override val merkleRoot: Hash,
    override val previousHash: Hash,
    override val blockParams: BlockParams,
    override val seconds: Long,
    override val nonce: Long
) : BlockHeader {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockHeader) return false

        if (chainHash != other.chainHash) return false
        if (previousHash != other.previousHash) return false
        if (blockParams != other.blockParams) return false
        if (merkleRoot != other.merkleRoot) return false
        if (seconds != other.seconds) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainHash.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + blockParams.hashCode()
        result = 31 * result + merkleRoot.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }


}