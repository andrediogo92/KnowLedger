package pt.um.lei.masb.blockchain.utils

/**
 * Indicates capability to produce a unique digest in string form.
 */
interface DigestAble {
    /**
     * Pure function that must produce a unique digest in string form through a DEFAULT_CRYPTER instance.
     */
    fun digest(c: Crypter): String
}