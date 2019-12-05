package org.knowledger.ledger.core.serial

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.time.Instant

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
        var seconds: Long? = null
        var nanos: Int? = null
        with(decoder.beginStructure(descriptor)) {

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
        }
        return Instant.ofEpochSecond(
            seconds ?: throw MissingFieldException("seconds"),
            nanos?.toLong() ?: throw MissingFieldException("nanos")
        )
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