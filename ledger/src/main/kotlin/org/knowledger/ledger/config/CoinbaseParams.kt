package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.hash.classDigest
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers.SHA3512Hasher
import org.knowledger.ledger.data.DefaultDiff
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.serial.internal.CoinbaseParamsByteSerializer
import org.knowledger.ledger.service.ServiceClass

@Serializable(with = CoinbaseParamsByteSerializer::class)
data class CoinbaseParams(
    val timeIncentive: Long = 5,
    val valueIncentive: Long = 2,
    val baseIncentive: Long = 3,
    val dividingThreshold: Long = 100000,
    val formula: Hash = classDigest<DefaultDiff>(SHA3512Hasher)
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}