package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.misc.toPublicKey
import java.security.PublicKey

@Serializer(forClass = PublicKey::class)
object PublicKeySerializer : KSerializer<PublicKey> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("publicKey") {
            init {
                addElement("bytes")
            }
        }

    override fun deserialize(decoder: Decoder): PublicKey =
        decoder
            .decodeByArray(descriptor)
            ?.toPublicKey()
            ?: throw MissingFieldException("bytes")


    override fun serialize(encoder: Encoder, obj: PublicKey) {
        encoder.encodeByArray(descriptor, obj.encoded)
    }
}