package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.base.data.Difficulty

@Serializer(forClass = Difficulty::class)
object DifficultySerializer : KSerializer<Difficulty> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Difficulty")

    override fun deserialize(decoder: Decoder): Difficulty =
        Difficulty(
            decoder.decodeSerializableValue(BigIntegerSerializer)
        )

    override fun serialize(encoder: Encoder, obj: Difficulty) {
        encoder.encodeSerializableValue(
            BigIntegerSerializer, obj.difficulty
        )
    }
}