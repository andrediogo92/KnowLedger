package pt.um.lei.masb.blockchain.utils

import mu.KLogging
import pt.um.lei.masb.blockchain.Hash
import java.security.MessageDigest

class SHA256Encrypter : Crypter {
    companion object : KLogging()

    /**
     * Applies Sha256 to a string [input] and returns the resulting [Hash].
     */
    override fun applyHash(input: String): Hash =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            //Applies sha256 to our input,
            digest
                .digest(input.toByteArray(Charsets.UTF_8))
                .toString()
        } catch (e: Exception) {
            logger.error(e) {}
            throw RuntimeException("Apply SHA256 problem", e)
        }

    override fun applyHash(input: ByteArray): Hash =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            //Applies sha256 to our input,
            digest
                .digest(input)
                .toString()
        } catch (e: Exception) {
            logger.error(e) {}
            throw RuntimeException("Apply SHA256 problem", e)
        }

    override val hashSize: Long = 32
}
