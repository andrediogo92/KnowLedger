package org.knowledger.ledger.crypto.hash

import kotlinx.serialization.cbor.Cbor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.knowledger.ledger.crypto.hash.Hashers.*
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer
import java.security.MessageDigest
import java.security.Security

/**
 * Hashers is a class hierarchy for implementations of
 * common digest algorithms. Out-of-the-box support for:
 * 1. 256-bit SHA3 -> [SHA3256Hasher]
 * 2. 512-bit SHA3 -> [SHA3512Hasher]
 * 3. 256-bit Haraka -> [Haraka256Hasher]
 * 4. 512-bit Haraka -> [Haraka512Hasher]
 * 5. 256-bit Blake2b -> [Blake2b256Hasher]
 * 6. 512-bit Blake2b -> [Blake2b512Hasher]
 * 7. 256-bit Blake2s -> [Blake2s256Hasher]
 * 8. 256-bit SHA2 -> [SHA256Hasher]
 * 9. 512-bit SHA2 -> [SHA512Hasher]
 * 10. 256-bit custom Keccak as per BouncyCastle's
 * custom Keccak parameters -> [Keccak256Hasher]
 * 11. 512-bit custom Keccak as per BouncyCastle's
 * custom Keccak parameters -> [Keccak512Hasher]
 */
sealed class Hashers(algorithmTag: String) : Hasher {
    val digester: MessageDigest by lazy {
        MessageDigest.getInstance(
            algorithmTag
        )
    }

    override val id: Hash by lazy {
        applyHash(Cbor.plain.dump(HashAlgorithmSerializer, this))
    }


    override fun applyHash(input: ByteArray): Hash =
        Hash(digester.digest(input))


    override val hashSize: Int by lazy {
        digester.digestLength
    }

    private fun checkAlgorithm(
        digestLength: Int, algorithm: String,
        providerName: String, providerVersion: Double
    ): Boolean =
        digestLength == hashSize &&
                algorithm == digester.algorithm &&
                providerName == digester.provider.name &&
                providerVersion == digester.provider.version


    object SHA3256Hasher : Hashers("SHA3-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object SHA3512Hasher : Hashers("SHA3-512") {
        override val hashSize: Int = BYTE_SIZE_512
    }

    object Haraka256Hasher : Hashers("HARAKA-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object Haraka512Hasher : Hashers("HARAKA-512") {
        override val hashSize: Int = BYTE_SIZE_512
    }

    object Blake2b256Hasher : Hashers("BLAKE2B-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object Blake2b512Hasher : Hashers("BLAKE2B-512") {
        override val hashSize: Int = BYTE_SIZE_512
    }

    object Blake2s256Hasher : Hashers("BLAKE2S-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object SHA256Hasher : Hashers("SHA-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object SHA512Hasher : Hashers("SHA-512") {
        override val hashSize: Int = BYTE_SIZE_512
    }

    object Keccak256Hasher : Hashers("KECCAK-256") {
        override val hashSize: Int = BYTE_SIZE_256
    }

    object Keccak512Hasher : Hashers("KECCAK-512") {
        override val hashSize: Int = BYTE_SIZE_512
    }


    companion object {
        private const val BYTE_SIZE_512 = 64
        private const val BYTE_SIZE_256 = 32
        val DEFAULT_HASHER = Blake2b256Hasher

        init {
            //Ensure Bouncy Castle Crypto provider is present
            if (Security.getProvider("BC") == null) {
                Security.addProvider(
                    BouncyCastleProvider()
                )
            }
        }

        /**
         * Returns the [Hashers] instance which matches the supplied
         * [hash] with the one generated by hashing its own contents.
         *
         * Throws [NoSuchHasherRegistered] if no matching [Hashers]
         * instance is present.
         */
        fun getHasher(hash: Hash): Hashers =
            when {
                SHA3256Hasher.checkForCrypter(hash) -> SHA3256Hasher
                SHA3512Hasher.checkForCrypter(hash) -> SHA3512Hasher
                Haraka256Hasher.checkForCrypter(hash) -> Haraka256Hasher
                Haraka512Hasher.checkForCrypter(hash) -> Haraka512Hasher
                Blake2b256Hasher.checkForCrypter(hash) -> Blake2b256Hasher
                Blake2b512Hasher.checkForCrypter(hash) -> Blake2b512Hasher
                Blake2s256Hasher.checkForCrypter(hash) -> Blake2s256Hasher
                SHA256Hasher.checkForCrypter(hash) -> SHA256Hasher
                SHA512Hasher.checkForCrypter(hash) -> SHA512Hasher
                Keccak256Hasher.checkForCrypter(hash) -> Keccak256Hasher
                Keccak512Hasher.checkForCrypter(hash) -> Keccak512Hasher
                else -> throw NoSuchHasherRegistered(hash)
            }

        /**
         * Checks one of the available algorithms matches
         * the provided parameters.
         * Returns the digest algorithm class from [Hashers]
         * implementers, if one such exists, that explicitly
         * matches in [digestLength], internal [algorithm] name,
         * [providerName] and [providerVersion] as specified
         * in Java Security API for [MessageDigest].
         */
        fun checkAlgorithms(
            digestLength: Int, algorithm: String,
            providerName: String, providerVersion: Double
        ): Hashers? =
            arrayOf(
                SHA256Hasher, SHA512Hasher,
                SHA3256Hasher, SHA3512Hasher,
                Haraka256Hasher, Haraka512Hasher,
                Blake2b256Hasher, Blake2b512Hasher,
                Blake2s256Hasher, Keccak256Hasher,
                Keccak512Hasher
            ).find {
                it.checkAlgorithm(
                    digestLength, algorithm,
                    providerName, providerVersion
                )
            }
    }
}