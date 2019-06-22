package pt.um.masb.common.hash

import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import java.security.MessageDigest
import java.security.Security


sealed class AvailableHashAlgorithms(algorithmTag: String) : Hasher {
    val digester: MessageDigest by lazy {
        MessageDigest.getInstance(
            algorithmTag
        )
    }

    override fun applyHash(input: ByteArray): Hash =
        Hash(digester.digest(input))


    override val hashSize: Int by lazy {
        digester.digestLength
    }

    override val id: Hash by lazy {
        val provider = digester.provider

        Hash(
            digester.digest(
                flattenBytes(
                    digester.digestLength.bytes(),
                    digester.algorithm.toByteArray(),
                    provider.name.toByteArray(),
                    provider.version.bytes()
                )
            )
        )
    }

    object SHA3256Hasher : AvailableHashAlgorithms("SHA3-256")
    object SHA3512Hasher : AvailableHashAlgorithms("SHA3-512")
    object Blake2b256Hasher : AvailableHashAlgorithms("BLAKE2B-256")
    object Blake2b512Hasher : AvailableHashAlgorithms("BLAKE2B-512")
    object Blake2s256Hasher : AvailableHashAlgorithms("BLAKE2S-256")
    object SHA256Hasher : AvailableHashAlgorithms("SHA-256")
    object SHA512Hasher : AvailableHashAlgorithms("SHA-512")
    object Keccak256Hasher : AvailableHashAlgorithms("KECCAK-256")
    object Keccak512Hasher : AvailableHashAlgorithms("KECCAK-512")

    companion object {
        class NoSuchHasherRegistered : Exception()

        init {
            //Ensure Bouncy Castle Crypto provider is present
            if (Security.getProvider("BC") == null) {
                Security.addProvider(
                    BouncyCastleProvider()
                )
            }
        }

        fun getHasher(hash: Hash): Hasher =
            when {
                SHA3256Hasher.checkForCrypter(hash) -> SHA3256Hasher
                SHA3512Hasher.checkForCrypter(hash) -> SHA3512Hasher
                Blake2b256Hasher.checkForCrypter(hash) -> Blake2b256Hasher
                Blake2b512Hasher.checkForCrypter(hash) -> Blake2b512Hasher
                Blake2s256Hasher.checkForCrypter(hash) -> Blake2s256Hasher
                SHA256Hasher.checkForCrypter(hash) -> SHA256Hasher
                SHA512Hasher.checkForCrypter(hash) -> SHA512Hasher
                Keccak256Hasher.checkForCrypter(hash) -> Keccak256Hasher
                Keccak512Hasher.checkForCrypter(hash) -> Keccak512Hasher
                else -> throw NoSuchHasherRegistered()
            }
    }
}

