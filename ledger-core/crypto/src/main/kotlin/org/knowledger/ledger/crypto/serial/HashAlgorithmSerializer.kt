package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.CompositeDecoder.Companion.READ_DONE
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.NoSuchHasherRegistered
import kotlin.properties.Delegates

object HashAlgorithmSerializer : KSerializer<Hashers> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("HashAlgorithm") {
            val digestLength = PrimitiveDescriptor("digestLength", PrimitiveKind.INT)
            val algorithm = PrimitiveDescriptor("algorithm", PrimitiveKind.STRING)
            val providerName = PrimitiveDescriptor("providerName", PrimitiveKind.STRING)
            val providerVersion = PrimitiveDescriptor("providerVersion", PrimitiveKind.DOUBLE)
            element(elementName = digestLength.serialName, descriptor = digestLength)
            element(elementName = algorithm.serialName, descriptor = algorithm)
            element(elementName = providerName.serialName, descriptor = providerName)
            element(elementName = providerVersion.serialName, descriptor = providerVersion)
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

    override fun serialize(encoder: Encoder, value: Hashers) {
        with(encoder.beginStructure(descriptor)) {
            encodeIntElement(descriptor, 0, value.digester.digestLength)
            encodeStringElement(descriptor, 1, value.digester.algorithm)
            encodeStringElement(descriptor, 2, value.digester.provider.name)
            encodeDoubleElement(descriptor, 3, value.digester.provider.version)
            endStructure(descriptor)
        }
    }
}