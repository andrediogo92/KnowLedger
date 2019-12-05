package org.knowledger.ledger.storage

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hasher
import org.knowledger.ledger.crypto.hash.Hashing

internal interface HashUpdateable : Hashing, Sizeable {
    fun updateHash(
        hasher: Hasher, encoder: BinaryFormat
    )

    fun recalculateHash(
        hasher: Hasher, encoder: BinaryFormat
    ): Hash

    fun recalculateSize(
        hasher: Hasher, encoder: BinaryFormat
    ): Long
}