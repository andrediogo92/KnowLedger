package org.knowledger.ledger.core.serial

import kotlinx.serialization.*
import java.time.Instant
import kotlin.properties.Delegates

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("instant") {
            val seconds = PrimitiveDescriptor("seconds", PrimitiveKind.LONG)
            val nanos = PrimitiveDescriptor("nanos", PrimitiveKind.LONG)
            element(elementName = seconds.serialName, descriptor = seconds)
            element(elementName = nanos.serialName, descriptor = nanos)
        }

    override fun deserialize(decoder: Decoder): Instant =
        compositeDecode(decoder) {
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
            Instant.ofEpochSecond(seconds, nanos.toLong())
        }

    override fun serialize(
        encoder: Encoder, value: Instant
    ) {
        compositeEncode(encoder) {
            encodeLongElement(
                descriptor, 0, value.epochSecond
            )
            encodeIntElement(
                descriptor, 1, value.nano
            )
        }
    }
}