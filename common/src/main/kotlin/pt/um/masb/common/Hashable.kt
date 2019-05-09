package pt.um.masb.common

import pt.um.masb.common.crypt.Crypter

/**
 * Indicates capability to produce
 * a unique digest of itself.
 */
interface Hashable {
    /**
     * Pure function that must produce a unique digest
     * through the use of a [Crypter] instance.
     */
    fun digest(c: Crypter): Hash
}