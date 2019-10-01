package org.knowledger.ledger.core.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.ByteArraySerializer

fun Decoder.decodeByArray(
    descriptor: SerialDescriptor
): ByteArray? {
    val dec: CompositeDecoder = beginStructure(descriptor)
    var bytes: ByteArray? = null
    loop@ while (true) {
        when (val i = dec.decodeElementIndex(descriptor)) {
            CompositeDecoder.READ_DONE -> break@loop
            0 -> bytes = dec.decodeSerializableElement(
                descriptor, i, ByteArraySerializer
            )
            else -> throw SerializationException("Unknown index $i")
        }
    }
    dec.endStructure(descriptor)
    return bytes
}

fun Encoder.encodeByArray(
    descriptor: SerialDescriptor,
    bytes: ByteArray
) {
    val enc: CompositeEncoder = beginStructure(descriptor)
    enc.encodeSerializableElement(
        descriptor, 0,
        ByteArraySerializer, bytes
    )
    enc.endStructure(descriptor)
}