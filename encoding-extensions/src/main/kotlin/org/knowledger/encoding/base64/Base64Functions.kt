package org.knowledger.encoding.base64

import org.apache.commons.codec.binary.Base64
import org.knowledger.encoding.EncodedString
import org.knowledger.encoding.truncatedEncode
import org.knowledger.ledger.core.data.ByteEncodable
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.hash.Hash
import java.math.BigInteger
import java.security.Key

private val b64 = Base64(0, null, true)

fun Key.base64Encoded(): Base64String = encoded.base64Encoded()

fun ByteEncodable.base64Encoded(): Base64String = bytes.base64Encoded()

fun String.base64Encoded(): Base64String = encodeToByteArray().base64Encoded()

fun ByteArray.base64Encoded(): Base64String = b64.encodeToString(this)

fun Base64String.base64Decoded(): ByteArray = b64.decode(this)

fun Base64String.base64DecodedToHash(): Hash = Hash(base64Decoded())

fun Base64String.base64DecodedToDifficulty(): Difficulty = Difficulty(BigInteger(base64Decoded()))

fun Base64String.base64DecodedToUTF8(): String = base64Decoded().decodeToString()

fun ByteEncodable.truncatedBase64Encoded(cutoffSize: Int = Hash.TRUNC): Base64String =
    truncatedEncode(cutoffSize, ByteArray::base64Encoded)

typealias Base64String = EncodedString