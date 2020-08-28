package org.knowledger.testing.logging

import org.knowledger.base64.base64Encoded
import org.tinylog.kotlin.Logger
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun <T, R> encodeAndDecode(
    data: T, encode: (T) -> R, decode: (R) -> T,
): Pair<T, R> {
    contract {
        callsInPlace(encode, InvocationKind.EXACTLY_ONCE)
        callsInPlace(decode, InvocationKind.EXACTLY_ONCE)
    }
    val resultingTransaction = encode(data)

    val rebuiltTransaction = decode(resultingTransaction)

    return rebuiltTransaction to resultingTransaction
}

inline fun <T> encodeAndDecodeText(
    data: T, encode: (T) -> String, decode: (String) -> T,
): Pair<T, String> {
    val result = encodeAndDecode(data, encode, decode)
    val (_, resultingTransaction) = result

    Logger.debug { resultingTransaction }
    Logger.debug {
        "Total Sequence Size -> ${resultingTransaction.length}"
    }
    return result
}

inline fun <T> encodeAndDecodeBinary(
    data: T, encode: (T) -> ByteArray, decode: (ByteArray) -> T,
): Pair<T, ByteArray> {
    val result = encodeAndDecode(data, encode, decode)
    val (_, resultingTransaction) = result

    Logger.debug(resultingTransaction::base64Encoded)
    Logger.debug {
        "Total Byte Size -> ${resultingTransaction.size}"
    }

    return result
}