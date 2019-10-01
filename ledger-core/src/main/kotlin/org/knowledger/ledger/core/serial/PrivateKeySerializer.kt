package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.misc.toPrivateKey
import java.security.PrivateKey

@Serializer(forClass = PrivateKey::class)
object PrivateKeySerializer : KSerializer<PrivateKey> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("privateKey") {
            init {
                addElement("bytes")
            }
        }

    override fun deserialize(decoder: Decoder): PrivateKey =
        decoder
            .decodeByArray(descriptor)
            ?.toPrivateKey()
            ?: throw MissingFieldException("bytes")


    override fun serialize(encoder: Encoder, obj: PrivateKey) {
        encoder.encodeByArray(descriptor, obj.encoded)
    }
}