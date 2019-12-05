package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey

@Serializer(forClass = KeyPair::class)
object KeyPairSerializer : KSerializer<KeyPair> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("keyPair") {
            init {
                addElement("private")
                addElement("public")
            }
        }

    override fun deserialize(decoder: Decoder): KeyPair {
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var private: PrivateKey? = null
        var public: PublicKey? = null
        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop
                0 -> private = dec.decodeSerializableElement(
                    descriptor, i,
                    PrivateKeySerializer
                )
                1 -> public = dec.decodeSerializableElement(
                    descriptor, i,
                    PublicKeySerializer
                )
                else -> throw SerializationException("Unknown index $i")
            }
        }
        dec.endStructure(descriptor)
        return KeyPair(
            public ?: throw MissingFieldException("private"),
            private ?: throw MissingFieldException("public")
        )
    }


    override fun serialize(encoder: Encoder, obj: KeyPair) {
        val enc: CompositeEncoder = encoder.beginStructure(descriptor)
        enc.encodeSerializableElement(
            descriptor, 0,
            PrivateKeySerializer, obj.private
        )
        enc.encodeSerializableElement(
            descriptor, 0,
            PublicKeySerializer, obj.public
        )
        enc.endStructure(descriptor)
    }
}