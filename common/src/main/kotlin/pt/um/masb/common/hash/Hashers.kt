package pt.um.masb.common.hash

import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import java.security.MessageDigest
import java.security.Security


sealed class AvailableHashAlgorithms : KLogging() {
    object SHA256Hasher : Hasher, AvailableHashAlgorithms() {

        val digester = MessageDigest.getInstance(
            "SHA-256"
        )

        /**
         * Applies Sha256 to an already hashed [input] and returns the resulting [Hash].
         */
        override fun applyHash(input: Hash): Hash =
            Hash(digester.digest(input.bytes))


        /**
         * Applies Sha256 to a string [input] and returns the resulting [Hash].
         */
        override fun applyHash(input: String): Hash =
            Hash(digester.digest(input.toByteArray()))

        /**
         * Applies Sha256 to a string [input] and returns the resulting [Hash].
         */
        override fun applyHash(input: ByteArray): Hash =
            Hash(digester.digest(input))


        override val hashSize: Long = 32

        override val id: Hash by lazy {
            val provider = digester.provider

            Hash(
                digester.digest(
                    flattenBytes(
                        digester.algorithm.toByteArray(),
                        provider.name.toByteArray(),
                        provider.version.bytes()
                    )
                )
            )
        }
    }

    companion object : KLogging() {
        //Ugly hack to ensure BC is loaded.
        private val force: Int =
            if (Security.getProvider("BC") == null) {
                Security.addProvider(BouncyCastleProvider())
            } else {
                0
            }


        fun getCrypter(hash: Hash): Hasher? =
            when {
                SHA256Hasher.checkForCrypter(hash) -> SHA256Hasher
                else -> null
            }
    }
}

