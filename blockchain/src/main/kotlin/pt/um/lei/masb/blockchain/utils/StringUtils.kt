package pt.um.lei.masb.blockchain.utils

import mu.KotlinLogging
import org.bouncycastle.jce.provider.BouncyCastleProvider
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
val DEFAULT_CRYPTER: Crypter =
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
        publicKey.encoded + data.digest(DEFAULT_CRYPTER)
    )


/**
 * Applies ECDSA Signature and returns the result (as bytes).
 */
fun applyECDSASig(
    privateKey: PrivateKey, input: String
): ByteArray =
    applyECDSASig(
        privateKey,
        input.toByteArray()
    )


/**
 * Applies ECDSA Signature and returns the result (as bytes).
 */
fun applyECDSASig(
    privateKey: PrivateKey,
    input: ByteArray
): ByteArray {
    val dsa = Signature.getInstance(
        "ECDSA",
        "BC"
    )
    dsa.initSign(privateKey)
    dsa.update(input)
    return dsa.sign()
}

/**
 * Verifies a String signature.
 */
fun verifyECDSASig(
    publicKey: PublicKey,
    data: String,
    signature: ByteArray
): Boolean =
    verifyECDSASig(
        publicKey,
        data.toByteArray(),
        signature
    )


/**
 * Verifies a ByteArray signature.
 */
fun verifyECDSASig(
    publicKey: PublicKey,
    data: ByteArray,
    signature: ByteArray
): Boolean {
    val ecdsaVerify = Signature.getInstance(
        "ECDSA",
        "BC"
    )
    ecdsaVerify.initVerify(publicKey)
    ecdsaVerify.update(data)
    return ecdsaVerify.verify(signature)
}


fun base64encode(
    toEncode: ByteArray
): String =
    Base64
        .getEncoder()
        .encodeToString(toEncode)

fun base64decode(
    toDecode: String
): ByteArray =
    Base64
        .getDecoder()
        .decode(toDecode)


fun getStringFromKey(
    key: Key
): String =
    base64encode(key.encoded)


/**
 * Accepts a [bytes] [ByteArray] encoded [PublicKey]
 * and returns the resulting ECDSA [PublicKey] via
 * an [X509EncodedKeySpec].
 */
fun byteEncodeToPublicKey(bytes: ByteArray): PublicKey =
    KeyFactory.getInstance(
        "ECDSA",
        "BC"
    ).generatePublic(
        X509EncodedKeySpec(bytes)
    )


/**
 * Accepts a [bytes] [ByteArray] encoded [PublicKey]
 * and returns the resulting ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun byteEncodeToPrivateKey(bytes: ByteArray): PrivateKey =
    KeyFactory.getInstance(
        "ECDSA",
        "BC"
    ).generatePrivate(
        PKCS8EncodedKeySpec(bytes)
    )


/**
 * Accepts an [s] [Base64] encoded string and returns
 * a ECDSA [PublicKey] via an [X509EncodedKeySpec].
 */
fun stringToPublicKey(s: String): PublicKey =
    byteEncodeToPublicKey(base64decode(s))


/**
 * Accepts an [s] [Base64] encoded string and returns
 * a ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun stringToPrivateKey(s: String): PrivateKey =
    byteEncodeToPrivateKey(base64decode(s))