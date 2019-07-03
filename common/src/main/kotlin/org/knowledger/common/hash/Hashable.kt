package org.knowledger.common.hash

/**
 * Indicates capability to produce
 * a unique digest of itself.
 */
interface Hashable {
    /**
     * Pure function that must produce a unique digest
     * through the use of a [Hasher] instance.
     */
    fun digest(c: Hasher): Hash
}