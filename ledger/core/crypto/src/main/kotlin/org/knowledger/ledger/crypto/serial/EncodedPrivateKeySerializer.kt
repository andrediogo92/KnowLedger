package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.encoding.base64.base64Decoded
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.crypto.EncodedPrivateKey

object EncodedPrivateKeySerializer : KSerializer<EncodedPrivateKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EncodedPrivateKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): EncodedPrivateKey =
        EncodedPrivateKey(decoder.decodeString().base64Decoded())

    override fun serialize(encoder: Encoder, value: EncodedPrivateKey) {
        encoder.encodeString(value.base64Encoded())
    }
}