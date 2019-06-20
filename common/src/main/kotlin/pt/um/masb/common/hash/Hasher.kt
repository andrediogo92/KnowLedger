package pt.um.masb.common.hash


/**
 * A simple interface for hashing a unique string
 * representation of value.
 */
interface Hasher {

    val hashSize: Long

    fun applyHash(input: String): Hash

    fun applyHash(input: ByteArray): Hash

    val id: Hash

    fun checkForCrypter(id: Hash): Boolean =
        id.contentEquals(this.id)

    fun applyHash(input: Hash): Hash
}
