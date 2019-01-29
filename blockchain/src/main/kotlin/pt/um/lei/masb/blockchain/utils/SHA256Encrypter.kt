package pt.um.lei.masb.blockchain.utils

import mu.KLogging
import pt.um.lei.masb.blockchain.Hash
import java.security.MessageDigest

class SHA256Encrypter : Crypter {
    companion object : KLogging()

    val digester = MessageDigest.getInstance(
        "SHA-256"
    )

    /**
     * Applies Sha256 to a string [input] and returns the resulting [Hash].
     */
    override fun applyHash(input: String): Hash =
        digester.digest(input.toByteArray())

    /**
     * Applies Sha256 to a string [input] and returns the resulting [Hash].
     */
    override fun applyHash(input: ByteArray): Hash =
        digester.digest(input)

    override val hashSize: Long = 32
}
