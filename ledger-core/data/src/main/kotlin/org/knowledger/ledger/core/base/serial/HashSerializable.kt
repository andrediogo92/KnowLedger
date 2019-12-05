package org.knowledger.ledger.core.base.serial

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.core.base.hash.Hashable
import org.knowledger.ledger.core.base.hash.Hasher

interface HashSerializable : Hashable {
    fun serialize(encoder: BinaryFormat): ByteArray

    override fun digest(c: Hasher, encoder: BinaryFormat): Hash =
        c.applyHash(serialize(encoder))

    fun approximateSize(encoder: BinaryFormat): Long =
        serialize(encoder).size.toLong()
}