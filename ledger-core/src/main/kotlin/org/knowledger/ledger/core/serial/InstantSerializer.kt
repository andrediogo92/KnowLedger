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
        val dec: CompositeDecoder = decoder.beginStructure(descriptor)
        var seconds: Long? = null
        var nanos: Int? = null
        loop@ while (true) {
            when (val i = dec.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop
                0 -> seconds = dec.decodeLongElement(
                    descriptor, i
                )
                1 -> nanos = dec.decodeIntElement(
                    descriptor, i
                )
                else -> throw SerializationException("Unknown index $i")
            }
        }
        dec.endStructure(descriptor)
        return Instant.ofEpochSecond(
            seconds ?: throw MissingFieldException("seconds"),
            nanos?.toLong() ?: throw MissingFieldException("nanos")
        )
    }

    override fun serialize(
        encoder: Encoder, obj: Instant
    ) {
        val enc: CompositeEncoder =
            encoder.beginStructure(descriptor)
        enc.encodeLongElement(
            descriptor, 0, obj.epochSecond
        )
        enc.encodeIntElement(
            descriptor, 1, obj.nano
        )
        enc.endStructure(descriptor)
    }
}