package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec

@JsonClass(generateAdapter = true)
data class Ident(
    val id: String,
    val pair: KeyPair
) : Storable,
    LedgerContract {


    val privateKey: PrivateKey
        get() = pair.private
    val publicKey: PublicKey
        get() = pair.public


    constructor(id: String) : this(id, generateNewKeyPair())


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Ident")
            .apply {
                setProperty(
                    "id",
                    id
                )
                setProperty(
                    "publicKey",
                    publicKey.encoded
                )
                setProperty(
                    "privateKey",
                    privateKey.encoded
                )
            }


    companion object : KLogging() {
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
