package org.knowledger.base64

import org.knowledger.ledger.core.base.hash.Hash
import java.security.Key
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun String.encodedStringInUTF8(): ByteArray = encodeToByteArray()

@Suppress("NOTHING_TO_INLINE")
@UseExperimental(ExperimentalStdlibApi::class)
inline fun ByteArray.decodedUTF8String(): String = decodeToString()

fun Key.base64Encoded(): String =
    encoded.base64Encoded()

fun Hash.base64Encoded(): String =
    bytes.base64Encoded()

fun String.base64Encoded(): String =
    encodedStringInUTF8().base64Encoded()

fun ByteArray.base64Encoded(): String =
    b64Encoder.encodeToString(this)

fun String.base64Decoded(): ByteArray =
    b64Decoder.decode(this)

fun String.base64DecodedToHash(): Hash =
    Hash(base64Decoded())

fun String.base64DecodedToUTF8(): String =
    base64Decoded().decodedUTF8String()