package pt.um.lei.masb.blockchain.utils

interface Crypter {
    fun applyHash(input: String): String

    val hashSize: Long
}
