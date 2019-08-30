package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.core.misc.bytesFromHexString
import org.knowledger.ledger.core.misc.toHexString
import org.knowledger.ledger.core.misc.toPrivateKey
import java.security.PrivateKey

@Serializer(forClass = PrivateKey::class)
object PrivateKeySerializer : KSerializer<PrivateKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("PrivateKey")

    override fun deserialize(
        decoder: Decoder
    ): PrivateKey =
    //decodes via string until byte array is able to be directly
        //decoded.
        decoder.decodeString().bytesFromHexString().toPrivateKey()


    override fun serialize(
        encoder: Encoder, obj: PrivateKey
    ) =
    //encodes via string until byte array is able to be directly
        //encoded.
        encoder.encodeString(
            obj.encoded.toHexString()
        )
}