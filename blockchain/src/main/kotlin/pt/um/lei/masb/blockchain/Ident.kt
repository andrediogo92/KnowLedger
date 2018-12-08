package pt.um.lei.masb.blockchain

import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.lei.masb.blockchain.Ident.KeyGenerationException
import pt.um.lei.masb.blockchain.persistance.IDENT
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
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
object Ident : KLogging() {


    lateinit var privateKey: PrivateKey
        private set


    lateinit var publicKey: PublicKey
        private set


    init {
        //Ensure Bouncy Castle Crypto provider is present
        if (Security.getProvider("BC") == null) {
            Security.addProvider(
                BouncyCastleProvider()
            )
        }
        if (!loadFromDB()) {
            try {
                generateNewIdent()
            } catch (e: GeneralSecurityException) {
                logger.error(e) {}
                throw KeyGenerationException(
                    "Keygen problem",
                    e
                )
            }

        }
    }


    private fun loadFromDB(): Boolean = let {
        val id = IDENT
        if (id == null) {
            false
        } else {
            privateKey =
                    stringToPrivateKey(
                        id.getProperty("privateKey")
                    )
            publicKey =
                    stringToPublicKey(
                        id.getProperty("publicKey")
                    )
            true
        }
    }


    private fun generateNewIdent() {
        val keygen = KeyPairGenerator.getInstance(
            "ECDSA",
            "BC"
        )
        val random = SecureRandom.getInstance(
            "SHA256PRNG"
        )
        val ecSpec = ECGenParameterSpec(
            "prime512v1"
        )
        // Initialize the key generator and generate a KeyPair
        keygen.initialize(ecSpec, random)
        //256 bytes provides an acceptable security level
        val keyPair = keygen.generateKeyPair()
        // Set the public and private keys from the keyPair
        privateKey = keyPair.private
        publicKey = keyPair.public

    }

    class KeyGenerationException(
        message: String,
        cause: Exception
    ) : Exception(message, cause)
}
