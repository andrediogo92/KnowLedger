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
import org.knowledger.ledger.core.misc.toPublicKey
import java.security.PublicKey

@Serializer(forClass = PublicKey::class)
object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor =
        StringDescriptor.withName("PublicKey")

    override fun deserialize(
        decoder: Decoder
    ): PublicKey =
    //decodes via string until byte array is able to be directly
        //decoded.
        decoder.decodeString().bytesFromHexString().toPublicKey()


    override fun serialize(
        encoder: Encoder, obj: PublicKey
    ) =
    //encodes via string until byte array is able to be directly
        //encoded.
        encoder.encodeString(
            obj.encoded.toHexString()
        )
}