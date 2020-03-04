package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.EncodedSignature

object EncodedSignatureSerializer : KSerializer<EncodedSignature> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("EncodedSignature", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): EncodedSignature =
        EncodedSignature(decoder.decodeString().base64Decoded())

    override fun serialize(encoder: Encoder, value: EncodedSignature) =
        encoder.encodeString(value.base64Encoded())
}