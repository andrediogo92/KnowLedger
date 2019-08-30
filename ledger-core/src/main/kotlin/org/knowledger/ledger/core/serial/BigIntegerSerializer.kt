package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.math.BigInteger

@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer : KSerializer<BigInteger> {
    override fun deserialize(
        decoder: Decoder
    ): BigInteger =
        BigInteger(decoder.decodeString())

    override fun serialize(
        encoder: Encoder, obj: BigInteger
    ) =
        encoder.encodeString(obj.toString())
}