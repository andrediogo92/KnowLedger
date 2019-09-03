package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.misc.classDigest
import org.knowledger.ledger.core.misc.hashFromHexString
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
        val hash = dec.decodeStringElement(descriptor, 0)
        dec.endStructure(descriptor)
        assert(
            hash.hashFromHexString() ==
                    DefaultDiff.classDigest(Hashers.SHA3512Hasher)
        )
        return DefaultDiff
    }

    override fun serialize(encoder: Encoder, obj: DefaultDiff) {
        val enc = encoder.beginStructure(descriptor)
        enc.encodeStringElement(
            descriptor, 0,
            DefaultDiff.classDigest(Hashers.SHA3512Hasher).print
        )
        enc.endStructure(descriptor)
    }
}