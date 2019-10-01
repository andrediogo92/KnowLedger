package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import java.math.BigInteger

@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("bigInteger") {
            init {
                addElement("bytes")
            }
        }

    override fun deserialize(decoder: Decoder): BigInteger =
        BigInteger(
            decoder.decodeByArray(descriptor)
                ?: throw MissingFieldException("bytes")
        )


    override fun serialize(encoder: Encoder, obj: BigInteger) {
        encoder.encodeByArray(descriptor, obj.toByteArray())
    }
}