package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.classDigest
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.ServiceClass

@Serializable
@SerialName("CoinbaseParams")
data class CoinbaseParams(
    val timeIncentive: Long = 5,
    val valueIncentive: Long = 2,
    val baseIncentive: Long = 3,
    val dividingThreshold: Long = 100000,
    val formula: Hash = DefaultDiff.classDigest(Hashers.SHA3512Hasher)
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}