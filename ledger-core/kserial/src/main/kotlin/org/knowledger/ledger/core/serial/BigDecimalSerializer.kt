package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import java.math.BigDecimal

@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("BigDecimal")

    override fun deserialize(
        decoder: Decoder
    ): BigDecimal =
        BigDecimal(decoder.decodeString())

    override fun serialize(
        encoder: Encoder, obj: BigDecimal
    ) =
        encoder.encodeString(obj.toEngineeringString())

}