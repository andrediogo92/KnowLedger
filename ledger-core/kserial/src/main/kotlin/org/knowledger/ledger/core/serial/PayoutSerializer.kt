package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import org.knowledger.ledger.core.base.data.Payout

@Serializer(forClass = Payout::class)
object PayoutSerializer : KSerializer<Payout> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("Payout", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Payout =
        Payout(
            decoder.decodeSerializableValue(
                BigDecimalSerializer
            )
        )

    override fun serialize(
        encoder: Encoder, value: Payout
    ) {
        encoder.encodeSerializableValue(
            BigDecimalSerializer, value.payout
        )
    }
}