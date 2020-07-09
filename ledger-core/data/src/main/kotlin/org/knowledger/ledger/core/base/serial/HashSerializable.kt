package org.knowledger.ledger.core.base.serial

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.base.hash.Hash
import org.knowledger.ledger.core.base.hash.Hashable
import org.knowledger.ledger.core.base.hash.Hasher

interface HashSerializable : Hashable {
    fun serialize(encoder: BinaryFormat): ByteArray

    override fun digest(hasher: Hasher, encoder: BinaryFormat): Hash =
        hasher.applyHash(serialize(encoder))
}