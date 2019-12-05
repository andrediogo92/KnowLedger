package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import java.util.*

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("UUID")

    override fun deserialize(decoder: Decoder): UUID =
        UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, obj: UUID) {
        encoder.encodeString(obj.toString())
    }
}