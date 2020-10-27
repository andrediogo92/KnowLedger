@file:OptIn(ExperimentalSerializationApi::class)
package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.encoding.base64.base64Decoded
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.encoding.trimBaseNPadding
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import java.math.BigInteger

fun <T : HashSerializable> T.calculateHash(hasher: Hashers, encoder: BinaryFormat): Hash =
    digest(hasher, encoder)

fun <T : HashSerializable> T.calculateSizeAndHash(
    hasher: Hashers, encoder: BinaryFormat
): Pair<Int, Hash> =
    with(serialize(encoder)) {
        val hash = hasher.applyHash(this)
        Pair(size + hash.bytes.size, hash)
    }

fun <T : HashSerializable> T.calculateApproximateSize(hasher: Hashers, encoder: BinaryFormat): Int =
    with(serialize(encoder)) {
        val hash = hasher.applyHash(this)
        size + hash.bytes.size
    }


fun Hash.toDifficulty(): Difficulty =
    Difficulty(BigInteger(bytes))

fun Hash.toTag(): Tag =
    Tag(base64Encoded().trimBaseNPadding())

fun Tag.toHash(): Hash =
    Hash(id.base64Decoded())

fun Difficulty.toHash(): Hash =
    Hash(bytes)