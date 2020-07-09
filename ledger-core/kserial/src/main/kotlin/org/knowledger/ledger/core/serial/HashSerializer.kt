package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.hash.Hash

@Serializer(forClass = Hash::class)
object HashSerializer : KSerializer<Hash> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor(serialName = "Hash", kind = PrimitiveKind.STRING)


    override fun deserialize(decoder: Decoder): Hash =
        Hash(decoder.decodeString().base64Decoded())


    override fun serialize(encoder: Encoder, value: Hash) {
        encoder.encodeString(value.base64Encoded())
    }
}