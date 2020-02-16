package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.NoSuchHasherRegistered
import kotlin.properties.Delegates

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
        return with(decoder.beginStructure(descriptor)) {
            var digestLength by Delegates.notNull<Int>()
            lateinit var algorithm: String
            lateinit var providerName: String
            var providerVersion by Delegates.notNull<Double>()
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    READ_DONE -> break@loop
                    0 -> digestLength = decodeIntElement(descriptor, i)
                    1 -> algorithm = decodeStringElement(descriptor, i)
                    2 -> providerName = decodeStringElement(descriptor, i)
                    3 -> providerVersion = decodeDoubleElement(descriptor, i)
                }
            }
            endStructure(descriptor)
            Hashers.checkAlgorithms(
                digestLength, algorithm,
                providerName, providerVersion
            ) ?: throw NoSuchHasherRegistered(
                digestLength, algorithm,
                providerName, providerVersion
            )
        }
    }

    override fun serialize(encoder: Encoder, obj: Hashers) {
        with(encoder.beginStructure(descriptor)) {
            encodeIntElement(descriptor, 0, obj.digester.digestLength)
            encodeStringElement(descriptor, 1, obj.digester.algorithm)
            encodeStringElement(descriptor, 2, obj.digester.provider.name)
            encodeDoubleElement(descriptor, 3, obj.digester.provider.version)
            endStructure(descriptor)
        }
    }
}