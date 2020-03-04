package org.knowledger.ledger.serial.display

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.base64.base64DecodedToDifficulty
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.data.Difficulty

internal object DifficultyDisplaySerializer : KSerializer<Difficulty> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("Difficulty", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Difficulty =
        decoder.decodeString().base64DecodedToDifficulty()

    override fun serialize(encoder: Encoder, value: Difficulty) {
        encoder.encodeString(
            value.base64Encoded()
        )
    }
}