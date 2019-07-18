package org.knowledger.ledger.core.misc

import org.knowledger.ledger.core.hash.Hash
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()

fun Hash.base64Encode(): String =
    bytes.base64Encode()


fun String.base64Encode(): String =
    encodeStringToUTF8().base64Encode()

fun ByteArray.base64Encode(): String =
    b64Encoder.encodeToString(this)

fun String.base64Decode(): ByteArray =
    b64Decoder.decode(this)

fun String.base64DecodeToHash(): Hash =
    Hash(base64Decode())

fun String.base64DecodeToString(): String =
    base64Decode().decodeUTF8ToString()