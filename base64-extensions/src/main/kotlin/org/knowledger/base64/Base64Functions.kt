package org.knowledger.base64

import org.knowledger.ledger.core.base.data.ByteEncodable
import org.knowledger.ledger.core.base.data.Difficulty
import org.knowledger.ledger.core.base.hash.Hash
import java.math.BigInteger
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

fun Key.base64Encoded(): Base64String =
    encoded.base64Encoded()

fun ByteEncodable.base64Encoded(): Base64String =
    bytes.base64Encoded()

fun String.base64Encoded(): Base64String =
    encodedStringInUTF8().base64Encoded()

fun ByteArray.base64Encoded(): Base64String =
    b64Encoder.encodeToString(this)

fun Base64String.base64Decoded(): ByteArray =
    b64Decoder.decode(this)

fun Base64String.base64DecodedToHash(): Hash =
    Hash(base64Decoded())

fun Base64String.base64DecodedToDifficulty(): Difficulty =
    Difficulty(BigInteger(base64Decoded()))

fun Base64String.base64DecodedToUTF8(): String =
    base64Decoded().decodedUTF8String()

typealias Base64String = String