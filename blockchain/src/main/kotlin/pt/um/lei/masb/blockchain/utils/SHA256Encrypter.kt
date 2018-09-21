package pt.um.lei.masb.blockchain.utils

import mu.KLogging
import java.security.MessageDigest
import kotlin.experimental.or

class SHA256Encrypter : Crypter {
    companion object : KLogging()

    //Applies Sha256 to a string and returns the result.
    override fun applyHash(input: String): String =
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            //Applies sha256 to our input,
            val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
            // This will contain the hash as hexadecimal
            val hexString = StringBuilder()
            for (bHash in hash) {
                val hex = Integer.toHexString((0xff.toByte() or bHash).toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            hexString.toString()
        } catch (e: Exception) {
            logger.error("", e.message)
            throw RuntimeException("Apply SHA256 problem", e)
        }

    override val hashSize: Long = 32
}
