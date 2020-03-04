package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.ByteArraySerializer
import org.knowledger.ledger.crypto.toPrivateKey
import java.security.PrivateKey

object PrivateKeySerializer : KSerializer<PrivateKey> {
    private val byteArraySerializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor =
        byteArraySerializer.descriptor

    override fun deserialize(decoder: Decoder): PrivateKey =
        decoder
            .decodeSerializableValue(byteArraySerializer)
            .toPrivateKey()


    override fun serialize(encoder: Encoder, value: PrivateKey) {
        encoder.encodeSerializableValue(
            byteArraySerializer, value.encoded
        )
    }
}