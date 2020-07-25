package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.toPublicKey
import java.security.PublicKey

object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("PublicKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PublicKey =
        decoder.decodeString().base64Decoded().toPublicKey()


    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeString(value.encoded.base64Encoded())
    }
}