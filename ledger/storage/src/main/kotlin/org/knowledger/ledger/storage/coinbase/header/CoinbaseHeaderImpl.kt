package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout

internal data class CoinbaseHeaderImpl(
    override val merkleRoot: Hash,
    override val payout: Payout,
    // Difficulty is fixed at mining time.
    override val blockheight: Long,
    override val difficulty: Difficulty,
    override val extraNonce: Long,
    override val coinbaseParams: CoinbaseParams,
) : CoinbaseHeader {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoinbaseHeader) return false

        if (merkleRoot != other.merkleRoot) return false
        if (payout != other.payout) return false
        if (blockheight != other.blockheight) return false
        if (difficulty != other.difficulty) return false
        if (extraNonce != other.extraNonce) return false
        if (coinbaseParams != other.coinbaseParams) return false
        return true
    }

    override fun hashCode(): Int {
        var result = merkleRoot.hashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + blockheight.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + extraNonce.hashCode()
        result = 31 * result + coinbaseParams.hashCode()
        return result
    }

}

