package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.classDigest
import org.knowledger.ledger.crypto.hash.Hashers

@Serializer(forClass = DefaultDiff::class)
object DefaultDataFormulaSerializer : KSerializer<DefaultDiff> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("DefaultDiff") {
            init {
                addElement("hash")
            }
        }

    override fun deserialize(decoder: Decoder): DefaultDiff {
        val dec = decoder.beginStructure(descriptor)
        val hash = dec.decodeSerializableElement(descriptor, 0, Hash.serializer())
        dec.endStructure(descriptor)
        assert(
            hash == DefaultDiff.classDigest(Hashers.SHA3512Hasher)
        )
        return DefaultDiff
    }

    override fun serialize(encoder: Encoder, obj: DefaultDiff) {
        val enc = encoder.beginStructure(descriptor)
        enc.encodeSerializableElement(
            descriptor, 0, Hash.serializer(),
            DefaultDiff.classDigest(Hashers.SHA3512Hasher)
        )
        enc.endStructure(descriptor)
    }
}