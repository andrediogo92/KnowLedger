package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.base.hash.bytesFromHexString
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.hash.toHexString

@Serializer(forClass = EncodedSignature::class)
object EncodedSignatureSerializer : KSerializer<EncodedSignature> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("EncodedSignature")

    override fun deserialize(decoder: Decoder): EncodedSignature =
        EncodedSignature(decoder.decodeString().bytesFromHexString())

    override fun serialize(encoder: Encoder, obj: EncodedSignature) =
        encoder.encodeString(obj.toHexString())
}