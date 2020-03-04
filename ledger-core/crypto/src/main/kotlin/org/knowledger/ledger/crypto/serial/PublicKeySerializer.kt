package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.ByteArraySerializer
import org.knowledger.ledger.crypto.toPublicKey
import java.security.PublicKey

object PublicKeySerializer : KSerializer<PublicKey> {
    private val byteArraySerializer = ByteArraySerializer()
    override val descriptor: SerialDescriptor =
        byteArraySerializer.descriptor

    override fun deserialize(decoder: Decoder): PublicKey =
        decoder
            .decodeSerializableValue(byteArraySerializer)
            .toPublicKey()


    override fun serialize(encoder: Encoder, value: PublicKey) {
        encoder.encodeSerializableValue(
            byteArraySerializer, value.encoded
        )
    }
}