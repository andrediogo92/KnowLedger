package org.knowledger.ledger.core.serial

import kotlinx.serialization.*
import java.util.*
import kotlin.properties.Delegates

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("UUID") {
            val higher = PrimitiveDescriptor("higher", PrimitiveKind.LONG)
            val lower = PrimitiveDescriptor("lower", PrimitiveKind.LONG)
            element(elementName = higher.serialName, descriptor = higher)
            element(elementName = lower.serialName, descriptor = lower)
        }

    override fun deserialize(decoder: Decoder): UUID =
        compositeDecode(decoder) {
            var higher by Delegates.notNull<Long>()
            var lower by Delegates.notNull<Long>()
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
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