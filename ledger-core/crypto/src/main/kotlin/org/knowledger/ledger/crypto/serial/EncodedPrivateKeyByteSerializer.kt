package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.ByteArraySerializer
import org.knowledger.ledger.crypto.EncodedPrivateKey

object EncodedPrivateKeyByteSerializer : KSerializer<EncodedPrivateKey> {
    private val byteArraySerializer = ByteArraySerializer()

    override val descriptor: SerialDescriptor =
        byteArraySerializer.descriptor

    override fun deserialize(decoder: Decoder): EncodedPrivateKey =
        EncodedPrivateKey(decoder.decodeSerializableValue(byteArraySerializer))

    override fun serialize(encoder: Encoder, value: EncodedPrivateKey) =
        encoder.encodeSerializableValue(byteArraySerializer, value.bytes)
}