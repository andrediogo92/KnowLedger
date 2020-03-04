package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.EncodedPublicKey

object EncodedPublicKeySerializer : KSerializer<EncodedPublicKey> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("EncodedPrivateKey", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): EncodedPublicKey =
        EncodedPublicKey(decoder.decodeString().base64Decoded())

    override fun serialize(encoder: Encoder, value: EncodedPublicKey) =
        encoder.encodeString(value.base64Encoded())
}