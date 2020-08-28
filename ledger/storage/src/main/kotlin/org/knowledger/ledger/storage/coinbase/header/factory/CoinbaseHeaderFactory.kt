package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.MutableCoinbaseHeader
import org.knowledger.ledger.storage.Payout

@OptIn(ExperimentalSerializationApi::class)
interface CoinbaseHeaderFactory : CloningFactory<MutableCoinbaseHeader> {
    fun create(
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
        merkleRoot: Hash = Hash.emptyHash, payout: Payout = Payout.ZERO,
        blockheight: Long = -1, difficulty: Difficulty = Difficulty.INIT_DIFFICULTY,
        extraNonce: Long = Long.MIN_VALUE,
    ): MutableCoinbaseHeader

    fun create(
        hash: Hash, merkleRoot: Hash, payout: Payout, blockheight: Long,
        difficulty: Difficulty, extraNonce: Long, coinbaseParams: CoinbaseParams,
    ): MutableCoinbaseHeader

    fun create(coinbase: CoinbaseHeader): MutableCoinbaseHeader
}