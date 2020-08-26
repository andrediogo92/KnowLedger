@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.data.Payout

@Serializer(forClass = Payout::class)
object PayoutSerializer : KSerializer<Payout> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Payout", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Payout =
        Payout(decoder.decodeSerializableValue(BigDecimalSerializer))

    override fun serialize(encoder: Encoder, value: Payout) {
        encoder.encodeSerializableValue(BigDecimalSerializer, value.payout)
    }
}