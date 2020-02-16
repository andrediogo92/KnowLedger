package org.knowledger.ledger.serial.display

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.base64.base64DecodedToDifficulty
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.data.Difficulty

internal object DifficultyDisplaySerializer : KSerializer<Difficulty> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Difficulty")

    override fun deserialize(decoder: Decoder): Difficulty =
        decoder.decodeString().base64DecodedToDifficulty()

    override fun serialize(encoder: Encoder, obj: Difficulty) {
        encoder.encodeString(
            obj.base64Encoded()
        )
    }
}