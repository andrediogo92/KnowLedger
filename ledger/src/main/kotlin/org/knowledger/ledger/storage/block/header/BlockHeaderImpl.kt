package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import java.time.Instant

internal data class BlockHeaderImpl(
    override val chainId: ChainId,
    override val previousHash: Hash,
    override val params: BlockParams,
    override val merkleRoot: Hash = Hash.emptyHash,
    override val seconds: Long = Instant.now().epochSecond,
    override val nonce: Long = Long.MIN_VALUE
) : BlockHeader {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockHeader) return false

        if (chainId != other.chainId) return false
        if (previousHash != other.previousHash) return false
        if (params != other.params) return false
        if (merkleRoot != other.merkleRoot) return false
        if (seconds != other.seconds) return false
        if (nonce != other.nonce) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chainId.hashCode()
        result = 31 * result + previousHash.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + merkleRoot.hashCode()
        result = 31 * result + seconds.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }


}