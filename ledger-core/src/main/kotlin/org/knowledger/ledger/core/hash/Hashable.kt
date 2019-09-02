package org.knowledger.ledger.core.hash

import kotlinx.serialization.cbor.Cbor

/**
 * Indicates capability to produce
 * a unique digest of itself.
 */
interface Hashable {
    /**
     * Pure function that must produce a unique digest
     * through the use of a [Hasher] instance and
     * a [Cbor] encoder.
     */
    fun digest(c: Hasher, cbor: Cbor): Hash
}