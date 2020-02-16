package org.knowledger.ledger.core.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import java.time.Instant
import kotlin.properties.Delegates

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("instant") {
            init {
                addElement("seconds")
                addElement("nanos")
            }
        }

    override fun deserialize(
        decoder: Decoder
    ): Instant {
        return with(decoder.beginStructure(descriptor)) {
            var seconds: Long by Delegates.notNull()
            var nanos: Int by Delegates.notNull()
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> seconds = decodeLongElement(
                        descriptor, i
                    )
                    1 -> nanos = decodeIntElement(
                        descriptor, i
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            Instant.ofEpochSecond(seconds, nanos.toLong())
        }
    }

    override fun serialize(
        encoder: Encoder, obj: Instant
    ) {
        with(encoder.beginStructure(descriptor)) {
            encodeLongElement(
                descriptor, 0, obj.epochSecond
            )
            encodeIntElement(
                descriptor, 1, obj.nano
            )
            endStructure(descriptor)
        }
    }
}