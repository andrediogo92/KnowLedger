package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.NoSuchHasherRegistered
import kotlin.properties.Delegates

@Serializer(forClass = Hashers::class)
object HashAlgorithmSerializer : KSerializer<Hashers> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("HashAlgorithm") {
            init {
                addElement("digestLength")
                addElement("algorithm")
                addElement("providerName")
                addElement("providerVersion")
            }
        }

    override fun deserialize(decoder: Decoder): Hashers {
        val dec = decoder.beginStructure(descriptor)
        var digestLength by Delegates.notNull<Int>()
        lateinit var algorithm: String
        lateinit var providerName: String
        var providerVersion by Delegates.notNull<Double>()
        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                READ_DONE -> break@loop
                0 -> digestLength = dec.decodeIntElement(descriptor, i)
                1 -> algorithm = dec.decodeStringElement(descriptor, i)
                2 -> providerName = dec.decodeStringElement(descriptor, i)
                3 -> providerVersion = dec.decodeDoubleElement(descriptor, i)
            }
        }
        return Hashers.checkAlgorithms(
            digestLength, algorithm,
            providerName, providerVersion
        ) ?: throw NoSuchHasherRegistered(
            digestLength, algorithm,
            providerName, providerVersion
        )
    }

    override fun serialize(encoder: Encoder, obj: Hashers) {
        val enc = encoder.beginStructure(descriptor)
        enc.encodeIntElement(descriptor, 0, obj.digester.digestLength)
        enc.encodeStringElement(descriptor, 1, obj.digester.algorithm)
        enc.encodeStringElement(descriptor, 2, obj.digester.provider.name)
        enc.encodeDoubleElement(descriptor, 3, obj.digester.provider.version)
        enc.endStructure(descriptor)
    }
}