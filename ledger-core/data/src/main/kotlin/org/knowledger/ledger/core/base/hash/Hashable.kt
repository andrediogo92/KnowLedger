package org.knowledger.ledger.core.base.hash

import kotlinx.serialization.BinaryFormat

/**
 * Indicates capability to produce
 * a unique digest of itself.
 */
interface Hashable {
    /**
     * Pure function that must produce a unique digest
     * through the use of a [Hasher] instance and
     * a [BinaryFormat] encoder.
     */
    fun digest(hasher: Hasher, encoder: BinaryFormat): Hash
}