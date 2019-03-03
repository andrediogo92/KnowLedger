package pt.um.lei.masb.blockchain.utils

import pt.um.lei.masb.blockchain.ledger.Hash

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
