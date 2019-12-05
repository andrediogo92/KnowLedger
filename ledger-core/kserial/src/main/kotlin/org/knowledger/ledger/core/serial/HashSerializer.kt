package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ByteArraySerializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.base.hash.Hash

@Serializer(forClass = Hash::class)
object HashSerializer : KSerializer<Hash> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Hash")

    override fun deserialize(decoder: Decoder): Hash =
        Hash(
            decoder.decodeSerializableValue(ByteArraySerializer)
        )


    override fun serialize(encoder: Encoder, obj: Hash) {
        encoder.encodeSerializableValue(
            ByteArraySerializer, obj.bytes
        )
    }
}