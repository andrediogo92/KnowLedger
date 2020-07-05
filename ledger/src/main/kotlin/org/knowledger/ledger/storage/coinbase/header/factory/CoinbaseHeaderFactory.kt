package org.knowledger.ledger.storage.coinbase.header.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.service.CloningFactory
import org.knowledger.ledger.storage.CoinbaseHeader
import org.knowledger.ledger.storage.MutableCoinbaseHeader

internal interface CoinbaseHeaderFactory :
    CloningFactory<MutableCoinbaseHeader> {
    fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hasher: Hashers,
        encoder: BinaryFormat
    ): MutableCoinbaseHeader

    fun create(
        coinbaseParams: CoinbaseParams,
        merkleRoot: Hash, payout: Payout,
        difficulty: Difficulty, blockheight: Long,
        extraNonce: Long, hash: Hash
    ): MutableCoinbaseHeader

    fun create(
        coinbase: CoinbaseHeader
    ): MutableCoinbaseHeader
}