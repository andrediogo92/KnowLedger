package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.compositeDecode
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.NoSuchHasherRegistered
import kotlin.properties.Delegates

@OptIn(ExperimentalSerializationApi::class)
object HashAlgorithmSerializer : KSerializer<Hashers> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("HashAlgorithm") {
            val hashSize = PrimitiveSerialDescriptor("digestLength", PrimitiveKind.INT)
            val algorithm = PrimitiveSerialDescriptor("algorithm", PrimitiveKind.STRING)
            element(elementName = hashSize.serialName, descriptor = hashSize)
            element(elementName = algorithm.serialName, descriptor = algorithm)
        }

    override fun deserialize(decoder: Decoder): Hashers =
        compositeDecode(decoder) {
            var hashSize by Delegates.notNull<Int>()
            lateinit var algorithm: String
            while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break
                    0 -> hashSize = decodeIntElement(descriptor, i)
                    1 -> algorithm = decodeStringElement(descriptor, i)
                }
            }
            Hashers.checkAlgorithms(
                hashSize, algorithm
            ) ?: throw NoSuchHasherRegistered(hashSize, algorithm)
        }

    override fun serialize(encoder: Encoder, value: Hashers) {
        compositeEncode(encoder) {
            encodeIntElement(descriptor, 0, value.hashSize)
            encodeStringElement(descriptor, 1, value.algorithmTag)
        }
    }
}