package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.crypto.EncodedPublicKey

object EncodedPublicKeySerializer : KSerializer<EncodedPublicKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("EncodedPrivateKey")

    override fun deserialize(decoder: Decoder): EncodedPublicKey =
        EncodedPublicKey(decoder.decodeString().base64Decoded())

    override fun serialize(encoder: Encoder, obj: EncodedPublicKey) =
        encoder.encodeString(obj.base64Encoded())
}