@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.knowledger.ledger.core.serial

import kotlinx.datetime.Instant
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
import kotlin.properties.Delegates

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("instant") {
            val seconds = PrimitiveSerialDescriptor("seconds", PrimitiveKind.LONG)
            val nanos = PrimitiveSerialDescriptor("nanos", PrimitiveKind.LONG)
            element(elementName = seconds.serialName, descriptor = seconds)
            element(elementName = nanos.serialName, descriptor = nanos)
        }

    override fun deserialize(decoder: Decoder): Instant =
        compositeDecode(decoder) {
            var seconds: Long by Delegates.notNull()
            var nanos: Int by Delegates.notNull()
            while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    DECODE_DONE -> break
                    0 -> seconds = decodeLongElement(descriptor, i)
                    1 -> nanos = decodeIntElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            Instant.fromEpochSeconds(seconds, nanos.toLong())
        }

    override fun serialize(encoder: Encoder, value: Instant) {
        compositeEncode(encoder) {
            encodeLongElement(descriptor, 0, value.epochSeconds)
            encodeIntElement(descriptor, 1, value.nanosecondsOfSecond)
        }
    }
}