package org.knowledger.ledger.core.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.data.hash.Hashable
import org.knowledger.ledger.core.data.hash.Hasher

@OptIn(ExperimentalSerializationApi::class)
interface HashSerializable : Hashable {
    fun serialize(encoder: BinaryFormat): ByteArray

    override fun digest(hasher: Hasher, encoder: BinaryFormat): Hash =
        hasher.applyHash(serialize(encoder))
}