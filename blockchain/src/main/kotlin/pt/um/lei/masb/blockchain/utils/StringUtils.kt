package pt.um.lei.masb.blockchain.utils

import mu.KotlinLogging
import pt.um.lei.masb.blockchain.data.PhysicalData
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val logger = KotlinLogging.logger {}
//Ugly hack to ensure BC is loaded.
internal val DEFAULT_CRYPTER: Crypter = if (Security.getProvider("BC") == null) {
    Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())
    SHA256Encrypter()
} else {
    SHA256Encrypter()
}


/**
 * Signs the sensor data using the private key.
 * @return Signature generated.
 */
fun generateSignature(privateKey: PrivateKey,
                      publicKey: PublicKey,
                      data: PhysicalData<*>): ByteArray =
        applyECDSASig(privateKey,
                      getStringFromKey(publicKey) +
                      //Marshall first.
                      data.digest(DEFAULT_CRYPTER))


/**
 * Applies ECDSA Signature and returns the result (as bytes).
 */
fun applyECDSASig(privateKey: PrivateKey, input: String): ByteArray =
        try {
            val dsa = Signature.getInstance("ECDSA", "BC")
            dsa.initSign(privateKey)
            val strByte = input.toByteArray()
            dsa.update(strByte)
            dsa.sign()
        } catch (e: GeneralSecurityException) {
            logger.error("", e.message)
            throw RuntimeException("ECDSA Signature problem", e)
    }

/**
 * Verifies a String signature.
 */
fun verifyECDSASig(publicKey: PublicKey, data: String, signature: ByteArray): Boolean =
        try {
            val ecdsaVerify = Signature.getInstance("ECDSA", "BC")
            ecdsaVerify.initVerify(publicKey)
            ecdsaVerify.update(data.toByteArray())
            ecdsaVerify.verify(signature)
        } catch (e: GeneralSecurityException) {
            logger.error("", e.message)
            throw RuntimeException("ECDSA Verification problem", e)
    }


fun getStringFromKey(key: Key): String =
        Base64.getEncoder().encodeToString(key.encoded)

fun stringToPublicKey(s: String): PublicKey =
        try {
            var c = Base64.getDecoder()
                .decode(s)
            var keyFact = KeyFactory.getInstance("ECDSA", "BC")
            var x509KeySpec = X509EncodedKeySpec(c)
            keyFact.generatePublic(x509KeySpec)
        } catch (e: GeneralSecurityException) {
            logger.error("", e.message)
            throw RuntimeException(e)
    }


fun getInitialDifficulty(): BigInteger {
    val targetbuilder = ByteArray(32)
    targetbuilder[0] = 0xE0.toByte()
    for (i in 1..32) {
        targetbuilder[i] = 0x0
    }
    return BigInteger(targetbuilder)
}
