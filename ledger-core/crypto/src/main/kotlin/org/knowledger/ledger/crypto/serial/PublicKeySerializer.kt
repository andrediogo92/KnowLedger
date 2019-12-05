package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.crypto.toPublicKey
import java.security.PublicKey

@Serializer(forClass = PublicKey::class)
object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("PublicKey")

    override fun deserialize(decoder: Decoder): PublicKey =
        decoder
            .decodeSerializableValue(ByteArraySerializer)
            .toPublicKey()


    override fun serialize(encoder: Encoder, obj: PublicKey) {
        encoder.encodeSerializableValue(
            ByteArraySerializer, obj.encoded
        )
    }
}