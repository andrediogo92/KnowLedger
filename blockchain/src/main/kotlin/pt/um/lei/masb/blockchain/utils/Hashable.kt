package pt.um.lei.masb.blockchain.utils

import pt.um.lei.masb.blockchain.Hash

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