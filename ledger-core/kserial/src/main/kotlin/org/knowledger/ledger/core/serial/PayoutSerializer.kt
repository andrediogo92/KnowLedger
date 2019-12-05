package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.base.data.Payout

@Serializer(forClass = Payout::class)
object PayoutSerializer : KSerializer<Payout> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Payout")

    override fun deserialize(decoder: Decoder): Payout =
        Payout(
            decoder.decodeSerializableValue(
                BigDecimalSerializer
            )
        )

    override fun serialize(
        encoder: Encoder, obj: Payout
    ) {
        encoder.encodeSerializableValue(
            BigDecimalSerializer, obj.payout
        )
    }
}