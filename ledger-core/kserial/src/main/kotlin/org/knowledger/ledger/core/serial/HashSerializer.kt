package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ByteArraySerializer
import org.knowledger.ledger.core.base.hash.Hash

@Serializer(forClass = Hash::class)
object HashSerializer : KSerializer<Hash> {
    private val byteArraySerializer = ByteArraySerializer()

    override val descriptor: SerialDescriptor =
        byteArraySerializer.descriptor


    override fun deserialize(decoder: Decoder): Hash =
        Hash(
            decoder.decodeSerializableValue(byteArraySerializer)
        )


    override fun serialize(encoder: Encoder, value: Hash) {
        encoder.encodeSerializableValue(
            byteArraySerializer, value.bytes
        )
    }
}