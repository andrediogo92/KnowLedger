package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout

internal data class CoinbaseHeaderImpl(
    override val coinbaseParams: CoinbaseParams,
    override val merkleRoot: Hash = Hash.emptyHash,
    override val payout: Payout = Payout.ZERO,
    // Difficulty is fixed at mining time.
    override val difficulty: Difficulty = Difficulty.MAX_DIFFICULTY,
    override val blockheight: Long = -1,
    override val extraNonce: Long = 0
) : CoinbaseHeader {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoinbaseHeader) return false

        if (payout != other.payout) return false
        if (difficulty != other.difficulty) return false
        if (blockheight != other.blockheight) return false
        if (extraNonce != other.extraNonce) return false
        if (coinbaseParams != other.coinbaseParams) return false
        return true
    }

    override fun hashCode(): Int {
        var result = payout.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + blockheight.hashCode()
        result = 31 * result + extraNonce.hashCode()
        result = 31 * result + coinbaseParams.hashCode()
        return result
    }

}

