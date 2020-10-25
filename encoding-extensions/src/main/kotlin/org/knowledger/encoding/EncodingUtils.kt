package org.knowledger.encoding

import org.knowledger.ledger.core.data.ByteEncodable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Builds an exactly sized [ByteArray] in place with elements up until
 * [toExclusive] index.
 * Does no bounds checking whatsoever.
 */
fun ByteArray.fastSlice(toExclusive: Int): ByteArray =
    ByteArray(toExclusive) {
        this@fastSlice[it]
    }

fun EncodedString.trimBaseNPadding(): EncodedString = trimEnd('=')

internal inline fun ByteEncodable.truncatedEncode(
    cutoffSize: Int, encode: (ByteArray) -> EncodedString,
): EncodedString {
    contract {
        callsInPlace(encode, InvocationKind.EXACTLY_ONCE)
    }
    val bytes = bytes
    return if (bytes.size > cutoffSize) {
        encode(bytes.fastSlice(cutoffSize))
    } else {
        encode(bytes)
    }
}

typealias EncodedString = String