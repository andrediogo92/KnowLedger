package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.toPrivateKey
import java.security.PrivateKey

object PrivateKeySerializer : KSerializer<PrivateKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("PrivateKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): PrivateKey =
        decoder.decodeString().base64Decoded().toPrivateKey()


    override fun serialize(encoder: Encoder, value: PrivateKey) {
        encoder.encodeString(value.encoded.base64Encoded())
    }
}