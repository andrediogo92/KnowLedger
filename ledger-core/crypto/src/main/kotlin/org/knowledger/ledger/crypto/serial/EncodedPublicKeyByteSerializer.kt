package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.ByteArraySerializer
import org.knowledger.ledger.crypto.EncodedPublicKey

object EncodedPublicKeyByteSerializer : KSerializer<EncodedPublicKey> {
    private val byteArraySerializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor =
        byteArraySerializer.descriptor

    override fun deserialize(decoder: Decoder): EncodedPublicKey =
        EncodedPublicKey(decoder.decodeSerializableValue(byteArraySerializer))

    override fun serialize(encoder: Encoder, value: EncodedPublicKey) =
        encoder.encodeSerializableValue(byteArraySerializer, value.bytes)
}