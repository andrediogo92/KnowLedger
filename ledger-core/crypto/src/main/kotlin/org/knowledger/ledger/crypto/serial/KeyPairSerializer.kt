package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

object KeyPairSerializer : KSerializer<KeyPair> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("keyPair") {
            init {
                addElement("private")
                addElement("public")
            }
        }

    override fun deserialize(decoder: Decoder): KeyPair {
        return with(decoder.beginStructure(descriptor)) {
            lateinit var private: PrivateKey
            lateinit var public: PublicKey
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> private = decodeSerializableElement(
                        descriptor, i,
                        PrivateKeySerializer
                    )
                    1 -> public = decodeSerializableElement(
                        descriptor, i,
                        PublicKeySerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            KeyPair(public, private)
        }
    }


    override fun serialize(encoder: Encoder, obj: KeyPair) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0,
                PrivateKeySerializer, obj.private
            )
            encodeSerializableElement(
                descriptor, 0,
                PublicKeySerializer, obj.public
            )
            endStructure(descriptor)
        }
    }
}