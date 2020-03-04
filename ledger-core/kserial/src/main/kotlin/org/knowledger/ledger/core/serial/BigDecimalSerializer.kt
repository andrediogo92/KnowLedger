package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import java.math.BigDecimal

@Serializer(forClass = BigDecimal::class)
object BigDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(
        decoder: Decoder
    ): BigDecimal =
        BigDecimal(decoder.decodeString())

    override fun serialize(
        encoder: Encoder, value: BigDecimal
    ) =
        encoder.encodeString(value.toEngineeringString())

}