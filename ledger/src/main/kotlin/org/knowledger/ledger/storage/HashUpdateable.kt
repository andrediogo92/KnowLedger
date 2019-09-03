package org.knowledger.ledger.storage

import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.Sizeable
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.hash.Hashing

internal interface HashUpdateable : Hashing, Sizeable {
    fun updateHash(
        hasher: Hasher, cbor: Cbor
    )

    fun recalculateHash(
        hasher: Hasher, cbor: Cbor
    ): Hash

    fun recalculateSize(
        hasher: Hasher, cbor: Cbor
    ): Long
}