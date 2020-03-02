package org.knowledger.ledger.storage

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.crypto.hash.Hashers

internal interface HashUpdateable : Hashing, Sizeable {
    fun updateHash(
        hasher: Hashers, encoder: BinaryFormat
    )

    fun recalculateHash(
        hasher: Hashers, encoder: BinaryFormat
    ): Hash

    fun recalculateSize(
        hasher: Hashers, encoder: BinaryFormat
    ): Long
}