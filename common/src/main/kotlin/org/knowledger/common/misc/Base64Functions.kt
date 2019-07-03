package org.knowledger.common.misc

import org.knowledger.common.hash.Hash
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()

fun base64Encode(
    toEncode: Hash
): String =
    base64Encode(toEncode.bytes)


fun base64Encode(
    toEncode: String
): String =
    base64Encode(toEncode.encodeStringToUTF8())

fun base64Encode(
    toEncode: ByteArray
): String =
    b64Encoder.encodeToString(toEncode)

fun base64Decode(
    toDecode: String
): ByteArray =
    b64Decoder.decode(toDecode)

fun base64DecodeToHash(
    toDecode: String
): Hash =
    Hash(base64Decode(toDecode))

fun base64DecodeToString(
    toDecode: String
): String =
    base64Decode(toDecode).decodeUTF8ToString()