package org.knowledger.ledger.crypto.hash

import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer
import java.security.MessageDigest
import java.security.Security

@Serializable(with = HashAlgorithmSerializer::class)
sealed class Hashers(algorithmTag: String) : Hasher {
    val digester: MessageDigest by lazy {
        MessageDigest.getInstance(
            algorithmTag
        )
    }

    override val id: Hash by lazy {
        Hash(Cbor.plain.dump(serializer(), this))
    }


    override fun applyHash(input: ByteArray): Hash =
        Hash(digester.digest(input))


    override val hashSize: Int by lazy {
        digester.digestLength
    }

    private fun checkAlgorithm(
        digestLength: Int, algorithm: String,
        providerName: String, providerVersion: Double
    ): Boolean = digestLength == hashSize &&
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

class NoSuchHasherRegistered() : Exception() {
    override var message: String? = super.message

    constructor(
        digestLength: Int,
        algorithm: String,
        providerName: String,
        providerVersion: Double
    ) : this() {
        message = """No hasher with: 
            |   Digest length -> $digestLength
            |   Algorithm -> $algorithm
            |   Provider -> $providerName
            |   Provider Version -> $providerVersion
            """.trimMargin()
    }

    constructor(
        hash: Hash
    ) : this() {
        message = "No hasher with hash: $hash"
    }
}