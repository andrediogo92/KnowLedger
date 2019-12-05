package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.crypto.toPrivateKey
import java.security.PrivateKey

@Serializer(forClass = PrivateKey::class)
object PrivateKeySerializer : KSerializer<PrivateKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("PrivateKey")

    override fun deserialize(decoder: Decoder): PrivateKey =
        decoder
            .decodeSerializableValue(ByteArraySerializer)
            .toPrivateKey()


    override fun serialize(encoder: Encoder, obj: PrivateKey) {
        encoder.encodeSerializableValue(
            ByteArraySerializer, obj.encoded
        )
    }
}