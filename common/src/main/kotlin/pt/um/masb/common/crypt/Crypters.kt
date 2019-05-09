package pt.um.masb.common.crypt

import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.masb.common.Hash
import java.security.MessageDigest
import java.security.Security

//Ugly hack to ensure BC is loaded.
private val force: Int =
    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider())
    } else {
        0
    }

sealed class AvailableCrypters : KLogging() {
    object SHA256Encrypter : Crypter, AvailableCrypters() {

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

        override val id: Hash by lazy {
            digester.digest(digester.algorithm.toByteArray())
        }
    }
}

