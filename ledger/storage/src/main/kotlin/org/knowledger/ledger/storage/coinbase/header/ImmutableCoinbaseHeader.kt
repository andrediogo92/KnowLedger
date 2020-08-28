@file:UseSerializers(HashSerializer::class, PayoutSerializer::class, DifficultySerializer::class)

package org.knowledger.ledger.storage.coinbase.header

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.DifficultySerializer
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.config.coinbase.ImmutableCoinbaseParams

@Serializable
@SerialName("CoinbaseHeader")
data class ImmutableCoinbaseHeader(
    override val hash: Hash,
    override val merkleRoot: Hash,
    override val payout: Payout,
    override val blockheight: Long,
    override val difficulty: Difficulty,
    override val extraNonce: Long,
    override val coinbaseParams: ImmutableCoinbaseParams,
) : HashedCoinbaseHeader