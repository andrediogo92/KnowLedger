package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.crypto.EncodedSignature

object EncodedSignatureByteSerializer : KSerializer<EncodedSignature> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("EncodedSignature")

    override fun deserialize(decoder: Decoder): EncodedSignature =
        EncodedSignature(decoder.decodeSerializableValue(ByteArraySerializer))

    override fun serialize(encoder: Encoder, obj: EncodedSignature) =
        encoder.encodeSerializableValue(ByteArraySerializer, obj.bytes)
}