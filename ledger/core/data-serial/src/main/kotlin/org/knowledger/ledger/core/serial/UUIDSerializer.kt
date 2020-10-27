@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*
import kotlin.properties.Delegates

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("UUID") {
            val higher = PrimitiveSerialDescriptor("higher", PrimitiveKind.LONG)
            val lower = PrimitiveSerialDescriptor("lower", PrimitiveKind.LONG)
            element(elementName = higher.serialName, descriptor = higher)
            element(elementName = lower.serialName, descriptor = lower)
        }

    override fun deserialize(decoder: Decoder): UUID =
        compositeDecode(decoder) {
            var higher by Delegates.notNull<Long>()
            var lower by Delegates.notNull<Long>()
            while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break
                    0 -> higher = decodeLongElement(descriptor, i)
                    1 -> lower = decodeLongElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            UUID(higher, lower)
        }

    override fun serialize(encoder: Encoder, value: UUID) {
        compositeEncode(encoder) {
            encodeLongElement(descriptor, 0, value.mostSignificantBits)
            encodeLongElement(descriptor, 1, value.leastSignificantBits)
        }
    }
}