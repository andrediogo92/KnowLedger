package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import java.math.BigInteger

@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("BigInteger")

    override fun deserialize(decoder: Decoder): BigInteger =
        BigInteger(
            decoder.decodeSerializableValue(
                ByteArraySerializer
            )
        )


    override fun serialize(encoder: Encoder, obj: BigInteger) {
        encoder.encodeSerializableValue(
            ByteArraySerializer, obj.toByteArray()
        )
    }
}