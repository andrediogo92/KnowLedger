package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout

internal data class HashedCoinbaseHeaderImpl(
    private var _hash: Hash,
    private var _merkleRoot: Hash,
    private var _payout: Payout,
    private var _blockheight: Long,
    private var _difficulty: Difficulty,
    private var _extraNonce: Long,
    override val coinbaseParams: CoinbaseParams,
) : MutableHashedCoinbaseHeader {
    override val hash: Hash get() = _hash

    override val merkleRoot: Hash get() = _merkleRoot

    override val payout: Payout get() = _payout

    override val blockheight: Long get() = _blockheight

    override val difficulty: Difficulty get() = _difficulty

    override val extraNonce: Long get() = _extraNonce


    override fun addToPayout(payout: Payout) {
        _payout += payout
    }

    override fun newNonce() {
        _extraNonce++
    }

    override fun updateMerkleRoot(merkleRoot: Hash) {
        _merkleRoot = merkleRoot
    }

    override fun updateHash(hash: Hash) {
        _hash = hash
    }


    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        _blockheight = blockheight
        _difficulty = difficulty
    }
}