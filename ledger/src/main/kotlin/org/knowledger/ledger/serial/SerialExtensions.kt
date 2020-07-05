package org.knowledger.ledger.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Encoder
import kotlinx.serialization.SerializationStrategy
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T, R : T> DeserializationStrategy<T>.compositeDecode(
    decoder: Decoder,
    decode: CompositeDecoder.() -> R
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
    encoder: Encoder,
    encode: CompositeEncoder.() -> Unit
) {
    contract {
        callsInPlace(encode, InvocationKind.EXACTLY_ONCE)
    }
    with(encoder.beginStructure(descriptor)) {
        encode()
        endStructure(descriptor)
    }
}
