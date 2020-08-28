package org.knowledger.ledger.storage.config.chainid

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams

data class ImmutableChainId(
    override val hash: Hash,
    override val ledgerHash: Hash,
    override val tag: Hash,
    override val blockParams: BlockParams,
    override val coinbaseParams: CoinbaseParams,
) : ChainId {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChainId) return false


        if (hash != other.hash) return false
        if (ledgerHash != other.ledgerHash) return false
        if (tag != other.tag) return false
        if (blockParams != other.blockParams) return false
        if (coinbaseParams != other.coinbaseParams) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + ledgerHash.hashCode()
        result = 31 * result + tag.hashCode()
        result = 31 * result + blockParams.hashCode()
        result = 31 * result + coinbaseParams.hashCode()
        return result
    }

}