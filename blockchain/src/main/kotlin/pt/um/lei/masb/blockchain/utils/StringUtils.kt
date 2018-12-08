package pt.um.lei.masb.blockchain.utils

import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.GeneralSecurityException
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val logger = KotlinLogging.logger {}

//Ugly hack to ensure BC is loaded.
internal val DEFAULT_CRYPTER: Crypter =
    if (Security.getProvider("BC") == null) {
        Security.addProvider(BouncyCastleProvider())
        SHA256Encrypter()
    } else {
        SHA256Encrypter()
    }


/**
 * Signs the sensor data using the private key.
 * @return Signature generated.
 */
fun generateSignature(
    privateKey: PrivateKey,
    publicKey: PublicKey,
    data: Hashable
): ByteArray =
    applyECDSASig(
        privateKey,
        getStringFromKey(publicKey) +
                //Marshall first.
                data.digest(DEFAULT_CRYPTER)
    )


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
        logger.error(e) {}
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
        logger.error(e) {}
        throw RuntimeException("ECDSA Verification problem", e)
    }

fun base64encode(toEncode: ByteArray): String =
    Base64.getEncoder().encodeToString(toEncode)

fun base64decode(toDecode: String): ByteArray =
    Base64.getDecoder().decode(toDecode)


fun getStringFromKey(key: Key): String =
    Base64.getEncoder().encodeToString(key.encoded)


/**
 * Accepts an [s] [Base64] encoded string and returns
 * a ECDSA [PublicKey] via an [X509EncodedKeySpec].
 */
fun stringToPublicKey(s: String): PublicKey =
    try {
        val c = Base64
            .getDecoder()
            .decode(s)
        val keyFact = KeyFactory.getInstance(
            "ECDSA",
            "BC"
        )
        val x509KeySpec = X509EncodedKeySpec(c)
        keyFact.generatePublic(x509KeySpec)
    } catch (e: GeneralSecurityException) {
        logger.error(e) {}
        throw e
    }


/**
 * Accepts an [s] [Base64] encoded string and returns
 * a ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun stringToPrivateKey(s: String): PrivateKey =
    try {
        val c = Base64
            .getDecoder()
            .decode(s)
        val keyFact =
            KeyFactory.getInstance(
                "ECDSA",
                "BC"
            )
        val pkcs8KeySpec = PKCS8EncodedKeySpec(c)
        keyFact.generatePrivate(pkcs8KeySpec)
    } catch (e: GeneralSecurityException) {
        logger.error(e) {}
        throw e
    }