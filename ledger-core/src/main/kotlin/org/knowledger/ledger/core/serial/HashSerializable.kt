package org.knowledger.ledger.core.serial

import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher

interface HashSerializable : Hashable {
    fun serialize(cbor: Cbor): ByteArray

    override fun digest(c: Hasher, cbor: Cbor): Hash =
        c.applyHash(serialize(cbor))

    fun approximateSize(cbor: Cbor): Long =
        serialize(cbor).size.toLong()
}