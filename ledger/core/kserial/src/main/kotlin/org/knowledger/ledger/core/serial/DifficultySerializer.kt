package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import org.knowledger.ledger.core.data.Difficulty

@Serializer(forClass = Difficulty::class)
object DifficultySerializer : KSerializer<Difficulty> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("Difficulty", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Difficulty =
        Difficulty(
            decoder.decodeSerializableValue(
                BigIntegerSerializer
            )
        )

    override fun serialize(encoder: Encoder, value: Difficulty) {
        encoder.encodeSerializableValue(BigIntegerSerializer, value.difficulty)
    }
}