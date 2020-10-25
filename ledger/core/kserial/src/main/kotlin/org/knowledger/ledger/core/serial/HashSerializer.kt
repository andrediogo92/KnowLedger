@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.encoding.base64.base64Decoded
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.core.data.hash.Hash

@Serializer(forClass = Hash::class)
object HashSerializer : KSerializer<Hash> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Hash", kind = PrimitiveKind.STRING)


    override fun deserialize(decoder: Decoder): Hash =
        Hash(decoder.decodeString().base64Decoded())


    override fun serialize(encoder: Encoder, value: Hash) {
        encoder.encodeString(value.base64Encoded())
    }
}