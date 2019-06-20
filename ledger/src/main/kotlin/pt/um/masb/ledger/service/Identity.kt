package pt.um.masb.ledger.service

import com.squareup.moshi.JsonClass
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.masb.common.storage.LedgerContract
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec

@JsonClass(generateAdapter = true)
data class Identity(
    val id: String,
    val pair: KeyPair
) : LedgerContract {


    val privateKey: PrivateKey
        get() = pair.private
    val publicKey: PublicKey
        get() = pair.public


    constructor(id: String) : this(id, generateNewKeyPair())


    companion object {
        init {
            //Ensure Bouncy Castle Crypto provider is present
            if (Security.getProvider("BC") == null) {
                Security.addProvider(
                    BouncyCastleProvider()
                )
            }
        }

        private val keygen by lazy {
            KeyPairGenerator.getInstance(
                "ECDSA",
                "BC"
            )
        }

        private val random by lazy {
            try {
                SecureRandom.getInstance("NativePRNGNonBlocking")
            } catch (e: Exception) {
                SecureRandom.getInstance("SHA1PRNG")
            }
        }

        private val ecSpec by lazy {
            ECGenParameterSpec(
                "P-521"
            )
        }

        private fun generateNewKeyPair(): KeyPair {
            // Initialize the key generator and generate a KeyPair
            keygen.initialize(
                ecSpec,
                random
            )
            //256 bytes provides an acceptable security level
            return keygen.generateKeyPair()
        }

    }
}
