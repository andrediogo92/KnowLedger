@file:UseSerializers(HashSerializer::class, PayoutSerializer::class, DifficultySerializer::class)

package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout

@Serializable
@SerialName("CoinbaseHeader")
data class ImmutableCoinbaseHeader(
    override val hash: Hash,
    override val payout: Payout,
    override val coinbaseParams: CoinbaseParams,
    override val merkleRoot: Hash,
    override val difficulty: Difficulty,
    override val blockheight: Long,
    override val extraNonce: Long
) : HashedCoinbaseHeader