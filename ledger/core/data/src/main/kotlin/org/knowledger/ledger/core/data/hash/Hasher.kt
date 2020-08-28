package org.knowledger.ledger.core.data.hash


/**
 * A simple interface for hashing a unique string
 * representation of value.
 */
interface Hasher {
    val id: Hash
    val hashSize: Int


    /**
     * Applies underlying hashing algorithm to [input]
     * and returns the resulting [Hash].
     */
    fun applyHash(input: String): Hash =
        applyHash(input.toByteArray())

    /**
     * Applies underlying hashing algorithm to [input]
     * and returns the resulting [Hash].
     */
    fun applyHash(input: ByteArray): Hash

    /**
     * Applies underlying hashing algorithm to [input]
     * and returns the resulting [Hash].
     */
    fun applyHash(input: Hash): Hash =
        applyHash(input.bytes)

    /**
     * Returns whether the [Hash] supplied in [id] matches this
     * [Hasher]'s own [Hasher.id]
     */
    fun checkForHasher(id: Hash): Boolean =
        id == this.id

}
