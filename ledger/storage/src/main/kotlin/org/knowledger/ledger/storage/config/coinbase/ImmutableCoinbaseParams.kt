@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.config.coinbase

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

@Serializable
@SerialName("CoinbaseParams")
data class ImmutableCoinbaseParams(
    override val hashSize: Int,
    override val timeIncentive: Long,
    override val valueIncentive: Long,
    override val baseIncentive: Long,
    override val dividingThreshold: Long,
    override val formula: Hash
) : CoinbaseParams {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoinbaseParams) return false

        if (hashSize != other.hashSize) return false
        if (timeIncentive != other.timeIncentive) return false
        if (valueIncentive != other.valueIncentive) return false
        if (baseIncentive != other.baseIncentive) return false
        if (dividingThreshold != other.dividingThreshold) return false
        if (formula != other.formula) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hashSize
        result = 31 * result + timeIncentive.hashCode()
        result = 31 * result + valueIncentive.hashCode()
        result = 31 * result + baseIncentive.hashCode()
        result = 31 * result + dividingThreshold.hashCode()
        result = 31 * result + formula.hashCode()
        return result
    }


}