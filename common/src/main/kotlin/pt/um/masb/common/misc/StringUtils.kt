package pt.um.masb.common.misc

import pt.um.masb.common.Hashable
import pt.um.masb.common.crypt.Crypter
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val b64Encoder = Base64.getEncoder()
private val b64Decoder = Base64.getDecoder()

/**
 * Signs the [data]'s digest appended to the [publicKey]
 * using the [privateKey].
 * Returns the generated signature as a [ByteArray].
 */
fun generateSignature(
    privateKey: PrivateKey,
    publicKey: PublicKey,
    data: Hashable,
    crypter: Crypter
): ByteArray =
    applyECDSASig(
        privateKey,
        publicKey.encoded + data.digest(crypter)
    )


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun applyECDSASig(
    privateKey: PrivateKey, input: String
): ByteArray =
    applyECDSASig(
        privateKey,
        input.toByteArray()
    )


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
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
 * Verifies a [String] signature.
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
 * Verifies a [ByteArray] signature.
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
    b64Encoder.encodeToString(toEncode)

fun base64decode(
    toDecode: String
): ByteArray =
    b64Decoder.decode(toDecode)


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