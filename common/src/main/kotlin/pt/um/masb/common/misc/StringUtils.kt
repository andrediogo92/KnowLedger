package pt.um.masb.common.misc

import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hasher
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*


private val hexCode = "0123456789ABCDEF".toCharArray()
private val b64Encoder = Base64.getUrlEncoder()
private val b64Decoder = Base64.getUrlDecoder()

/**
 * Signs the [data]'s digest appended to the [publicKey]
 * using the [privateKey].
 * Returns the generated signature as a [ByteArray].
 */
fun generateSignature(
    privateKey: PrivateKey,
    publicKey: PublicKey,
    data: Hashable,
    hasher: Hasher
): ByteArray =
    applyECDSASig(
        privateKey,
        publicKey.encoded + data.digest(hasher).bytes
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


fun base64Encode(
    toEncode: Hash
): String =
    b64Encoder.encodeToString(toEncode.bytes)


fun base64Encode(
    toEncode: ByteArray
): String =
    b64Encoder.encodeToString(toEncode)

fun base64Decode(
    toDecode: String
): ByteArray =
    b64Decoder.decode(toDecode)

fun base64DecodeToHash(
    toDecode: String
): Hash =
    Hash(b64Decoder.decode(toDecode))

fun getStringFromKey(
    key: Key
): String =
    base64Encode(key.encoded)


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
    byteEncodeToPublicKey(base64Decode(s))


/**
 * Accepts an [s] [Base64] encoded string and returns
 * a ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun stringToPrivateKey(s: String): PrivateKey =
    byteEncodeToPrivateKey(base64Decode(s))


val String.hashFromHexString: Hash
    get() = Hash(parseHexBinary(this))


fun parseHexBinary(s: String): ByteArray {
    val len = s.length

    // "111" is not a valid hex encoding.
    if (len % 2 != 0) throw IllegalArgumentException(
        "hexBinary needs to be even-length: $s"
    )

    val out = ByteArray(len / 2)

    var i = 0
    while (i < len) {
        val h = hexToBin(s[i])
        val l = hexToBin(s[i + 1])
        if (h == -1 || l == -1) throw IllegalArgumentException(
            "contains illegal character for hexBinary: $s"
        )

        out[i / 2] = (h * 16 + l).toByte()
        i += 2
    }

    return out
}

private fun hexToBin(ch: Char): Int =
    when (ch) {
        in '0'..'9' -> ch - '0'
        in 'A'..'F' -> ch - 'A' + 10
        in 'a'..'f' -> ch - 'a' + 10
        else -> -1
    }

fun printHexBinary(data: ByteArray): String {
    val r = CharArray(data.size * 2)
    for (j in 0 until data.size) {
        val v = data[j].toInt() and 0xFF
        r[j * 2] = hexCode[v.ushr(4)]
        r[j * 2 + 1] = hexCode[v and 0x0F]
    }
    return String(r)
}