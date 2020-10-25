package org.knowledger.encoding.base32

import org.apache.commons.codec.binary.Base32
import org.knowledger.encoding.EncodedString
import org.knowledger.encoding.truncatedEncode
import org.knowledger.ledger.core.data.ByteEncodable
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.hash.Hash
import java.math.BigInteger
import java.security.Key

private val b32 = Base32(true)

fun Key.base32Encoded(): Base32String = encoded.base32Encoded()

fun ByteEncodable.base32Encoded(): Base32String = bytes.base32Encoded()

fun String.base32Encoded(): Base32String = encodeToByteArray().base32Encoded()

fun ByteArray.base32Encoded(): Base32String = b32.encodeToString(this)

fun Base32String.base32Decoded(): ByteArray = b32.decode(this)

fun Base32String.base32DecodedToHash(): Hash = Hash(base32Decoded())

fun Base32String.base32DecodedToDifficulty(): Difficulty = Difficulty(BigInteger(base32Decoded()))

fun Base32String.base32DecodedToUTF8(): String = base32Decoded().decodeToString()

fun ByteEncodable.truncatedBase32Encoded(cutoffSize: Int = Hash.TRUNC): Base32String =
    truncatedEncode(cutoffSize, ByteArray::base32Encoded)


typealias Base32String = EncodedString