package org.knowledger.ledger.core.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import java.util.*
import kotlin.properties.Delegates

@Serializer(forClass = UUID::class)
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("UUID") {
            init {
                addElement(name = "higher")
                addElement(name = "lower")
            }
        }

    override fun deserialize(decoder: Decoder): UUID {
        return with(decoder.beginStructure(descriptor)) {
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
            endStructure(descriptor)
            UUID(higher, lower)
        }
    }

    override fun serialize(encoder: Encoder, obj: UUID) {
        with(encoder.beginStructure(descriptor)) {
            encodeLongElement(
                descriptor, 0, obj.mostSignificantBits
            )
            encodeLongElement(
                descriptor, 1, obj.leastSignificantBits
            )
            endStructure(descriptor)
        }
    }
}