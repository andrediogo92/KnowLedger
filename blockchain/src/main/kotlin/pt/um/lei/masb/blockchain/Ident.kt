package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec

object Ident :
    KLogging(),
    Storable,
    BlockChainContract {


    lateinit var privateKey: PrivateKey
        private set

    lateinit var publicKey: PublicKey
        private set

    private val keygen = KeyPairGenerator.getInstance(
        "ECDSA",
        "BC"
    )

    private val random = SecureRandom.getInstanceStrong()

    private val ecSpec = ECGenParameterSpec(
        "P-521"
    )


    init {
        //Ensure Bouncy Castle Crypto provider is present
        if (Security.getProvider("BC") == null) {
            Security.addProvider(
                BouncyCastleProvider()
            )
        }
        if (!loadFromDB()) {
            val (prKey, pubKey) = generateNewIdent()
            privateKey = prKey
            publicKey = pubKey

        }
    }


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Ident")
            .apply {
                this.setProperty(
                    "publicKey",
                    publicKey.encoded
                )
                this.setProperty(
                    "privateKey",
                    privateKey.encoded
                )
            }


    private fun loadFromDB(): Boolean = let {
        val id: OElement? = null//ident
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

    fun generateNewIdent(): Pair<PrivateKey, PublicKey> {
        // Initialize the key generator and generate a KeyPair
        keygen.initialize(ecSpec, random)
        //256 bytes provides an acceptable security level
        val keyPair = keygen.generateKeyPair()
        // Set the public and private keys from the keyPair
        return Pair(keyPair.private, keyPair.public)
    }
}
