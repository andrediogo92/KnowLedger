@file:OptIn(ExperimentalSerializationApi::class)
package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers

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
