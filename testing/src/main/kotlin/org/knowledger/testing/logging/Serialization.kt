package org.knowledger.testing.logging

import org.knowledger.base64.base64Encoded
import org.tinylog.kotlin.Logger

inline fun <T> encodeAndDecodeText(
    data: T, encode: (T) -> String, decode: (String) -> T
): Pair<T, String> {
    val resultingTransaction = encode(data)

    Logger.debug { resultingTransaction }
    Logger.debug {
        "Total Sequence Size -> ${resultingTransaction.length}"
    }

    val rebuiltTransaction = decode(resultingTransaction)

    return rebuiltTransaction to resultingTransaction
}

inline fun <T> encodeAndDecodeBinary(
    data: T, encode: (T) -> ByteArray, decode: (ByteArray) -> T
): Pair<T, ByteArray> {
    val resultingTransaction = encode(data)

    Logger.debug { resultingTransaction.base64Encoded() }
    Logger.debug {
        "Total Byte Size -> ${resultingTransaction.size}"
    }

    val rebuiltTransaction = decode(resultingTransaction)

    return rebuiltTransaction to resultingTransaction
}