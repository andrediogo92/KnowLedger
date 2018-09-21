package pt.um.lei.masb.blockchain

import mu.KLogging
import pt.um.lei.masb.blockchain.Ident.KeyGenerationException
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec

/**
 * @throws KeyGenerationException When key generation fails.
 */
class Ident {
    companion object : KLogging() {
        //Ensure Bouncy Castle Crypto provider is present
        init {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
            }
        }
    }

    class KeyGenerationException(message: String, cause: Exception) : Exception(message, cause)


    val privateKey: PrivateKey

    val publicKey: PublicKey

    init {
        try {
            val keygen = KeyPairGenerator.getInstance("ECDSA", "BC")
            val random = SecureRandom.getInstance("SHA1PRNG")
            val ecSpec = ECGenParameterSpec("prime192v1")
            // Initialize the key generator and generate a KeyPair
            keygen.initialize(ecSpec, random)
            //256 bytes provides an acceptable security level
            val keyPair = keygen.generateKeyPair()
            // Set the public and private keys from the keyPair
            privateKey = keyPair.private
            publicKey = keyPair.public
        } catch (e: GeneralSecurityException) {
            logger.error("", e.message)
            throw KeyGenerationException("Keygen problem", e)
        }
    }
}
