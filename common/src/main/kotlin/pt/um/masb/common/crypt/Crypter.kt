package pt.um.masb.common.crypt

import pt.um.masb.common.Hash

/**
 * A simple interface for hashing a unique string
 * representation of data.
 */
interface Crypter {

    val hashSize: Long

    fun applyHash(input: String): Hash

    fun applyHash(input: ByteArray): Hash

    val id: Hash
}
