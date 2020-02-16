package org.knowledger.ledger.serial.display

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.base.hash.Hash

internal object HashDisplaySerializer : KSerializer<Hash> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("Hash")

    override fun deserialize(decoder: Decoder): Hash =
        decoder.decodeString().base64DecodedToHash()


    override fun serialize(encoder: Encoder, obj: Hash) {
        encoder.encodeString(obj.base64Encoded())
    }
}