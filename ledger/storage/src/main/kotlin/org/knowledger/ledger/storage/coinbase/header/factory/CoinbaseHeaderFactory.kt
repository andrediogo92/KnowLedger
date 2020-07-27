package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout

interface CoinbaseHeaderFactory :
    CloningFactory<MutableCoinbaseHeader> {
    fun create(
        merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams,
        hasher: Hashers, encoder: BinaryFormat,
        payout: Payout = Payout.ZERO,
        difficulty: Difficulty = Difficulty.INIT_DIFFICULTY,
        extraNonce: Long = Long.MIN_VALUE
    ): MutableCoinbaseHeader

    fun create(
        hash: Hash, merkleRoot: Hash, blockheight: Long,
        coinbaseParams: CoinbaseParams,
        payout: Payout = Payout.ZERO,
        difficulty: Difficulty = Difficulty.INIT_DIFFICULTY,
        extraNonce: Long = Long.MIN_VALUE
    ): MutableCoinbaseHeader

    fun create(
        coinbase: CoinbaseHeader
    ): MutableCoinbaseHeader
}