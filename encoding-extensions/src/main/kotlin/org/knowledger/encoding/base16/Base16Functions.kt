package org.knowledger.encoding.base16

import org.apache.commons.codec.binary.Base16
import org.knowledger.encoding.EncodedString
import org.knowledger.encoding.truncatedEncode
import org.knowledger.ledger.core.data.ByteEncodable
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.hash.Hash
import java.math.BigInteger
import java.security.Key

private val b16 = Base16()

fun Key.hexEncoded(): HexString = encoded.hexEncoded()

fun ByteEncodable.hexEncoded(): HexString = bytes.hexEncoded()

fun String.hexEncoded(): HexString = encodeToByteArray().hexEncoded()

fun ByteArray.hexEncoded(): HexString = b16.encodeToString(this)

fun HexString.hexDecoded(): ByteArray = b16.decode(this)

fun HexString.hexDecodedToHash(): Hash = Hash(hexDecoded())

fun HexString.hexDecodedToDifficulty(): Difficulty = Difficulty(BigInteger(hexDecoded()))

fun HexString.hexDecodedToUTF8(): String = hexDecoded().decodeToString()

fun ByteEncodable.truncatedHexEncoded(cutoffSize: Int = Hash.TRUNC): HexString =
    truncatedEncode(cutoffSize, ByteArray::hexEncoded)

typealias HexString = EncodedString