package org.knowledger.ledger.core.misc

import org.knowledger.ledger.core.hash.Hash
import java.util.*

private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()

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