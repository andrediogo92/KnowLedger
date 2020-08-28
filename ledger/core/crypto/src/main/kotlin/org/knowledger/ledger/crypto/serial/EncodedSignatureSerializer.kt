package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.EncodedSignature

object EncodedSignatureSerializer : KSerializer<EncodedSignature> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EncodedSignature", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): EncodedSignature =
        EncodedSignature(decoder.decodeString().base64Decoded())

    override fun serialize(encoder: Encoder, value: EncodedSignature) {
        encoder.encodeString(value.base64Encoded())
    }
}