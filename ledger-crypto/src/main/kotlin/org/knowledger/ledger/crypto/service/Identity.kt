@file:UseSerializers(PrivateKeySerializer::class, PublicKeySerializer::class)

package org.knowledger.ledger.crypto.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.knowledger.ledger.core.serial.PrivateKeySerializer
import org.knowledger.ledger.core.serial.PublicKeySerializer
import org.knowledger.ledger.core.storage.LedgerContract
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec

@Serializable
data class Identity(
    val id: String,
    val privateKey: PrivateKey,
    val publicKey: PublicKey
) : LedgerContract {
    constructor(id: String) : this(id, generateNewKeyPair())

    constructor(
        id: String, keyPair: KeyPair
    ) : this(id, keyPair.private, keyPair.public)

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
