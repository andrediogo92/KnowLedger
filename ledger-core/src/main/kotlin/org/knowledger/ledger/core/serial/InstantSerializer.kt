package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.time.Instant

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override fun deserialize(
        decoder: Decoder
    ): Instant =
        Instant.parse(decoder.decodeString())

    override fun serialize(
        encoder: Encoder, obj: Instant
    ) =
        encoder.encodeString(obj.toString())
}