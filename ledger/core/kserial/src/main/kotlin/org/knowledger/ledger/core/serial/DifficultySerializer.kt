@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.data.Difficulty

@Serializer(forClass = Difficulty::class)
object DifficultySerializer : KSerializer<Difficulty> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Difficulty", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Difficulty =
        Difficulty(decoder.decodeSerializableValue(BigIntegerSerializer))

    override fun serialize(encoder: Encoder, value: Difficulty) {
        encoder.encodeSerializableValue(BigIntegerSerializer, value.difficulty)
    }
}