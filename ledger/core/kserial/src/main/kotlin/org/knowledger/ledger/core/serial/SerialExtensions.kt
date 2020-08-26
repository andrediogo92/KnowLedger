package org.knowledger.ledger.core.serial

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T, R : T> DeserializationStrategy<T>.compositeDecode(
    decoder: Decoder, decode: CompositeDecoder.() -> R
): R {
    contract {
        callsInPlace(decode, InvocationKind.EXACTLY_ONCE)
    }
    return with(decoder.beginStructure(descriptor)) {
        val result = decode()
        endStructure(descriptor)
        result
    }
}

inline fun <T> SerializationStrategy<T>.compositeEncode(
    encoder: Encoder, encode: CompositeEncoder.() -> Unit
) {
    contract {
        callsInPlace(encode, InvocationKind.EXACTLY_ONCE)
    }
    with(encoder.beginStructure(descriptor)) {
        encode()
        endStructure(descriptor)
    }
}
