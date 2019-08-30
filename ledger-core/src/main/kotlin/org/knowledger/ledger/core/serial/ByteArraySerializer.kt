package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import org.knowledger.ledger.core.misc.bytesFromHexString
import org.knowledger.ledger.core.misc.toHexString

@Serializer(forClass = ByteArray::class)
object ByteArraySerializer : KSerializer<ByteArray> {
    override val descriptor: SerialDescriptor =
        StringDescriptor

    override fun deserialize(decoder: Decoder): ByteArray =
        decoder.decodeString().bytesFromHexString()

    override fun serialize(encoder: Encoder, obj: ByteArray) =
        encoder.encodeString(obj.toHexString())
}