package org.knowledger.ledger.core.misc

import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.hash.Hashable
import org.knowledger.ledger.core.hash.Hasher
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

private val keyFactory: KeyFactory =
    KeyFactory.getInstance(
        "ECDSA",
        "BC"
    )

/**
 * Signs the [data]'s digest appended to the [publicKey]
 * using the [privateKey].
 * Returns the generated signature as a [ByteArray].
 */
fun generateSignature(
    privateKey: PrivateKey,
    publicKey: PublicKey,
    data: Hashable,
    hasher: Hasher,
    cbor: Cbor
): ByteArray =
    applyECDSASig(
        privateKey,
        publicKey.encoded + data.digest(hasher, cbor).bytes
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

/**
 * Accepts a [toBytes] [ByteArray] encoded [PublicKey]
 * and returns the resulting ECDSA [PublicKey] via
 * an [X509EncodedKeySpec].
 */
fun ByteArray.toPublicKey(): PublicKey =
    keyFactory.generatePublic(
        X509EncodedKeySpec(this)
    )


/**
 * Accepts a [toBytes] [ByteArray] encoded [PublicKey]
 * and returns the resulting ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun ByteArray.toPrivateKey(): PrivateKey =
    keyFactory.generatePrivate(
        PKCS8EncodedKeySpec(this)
    )


/**
 * Accepts a base64 encoded string and returns
 * a ECDSA [PublicKey] via an [X509EncodedKeySpec].
 */
fun String.toPublicKey(): PublicKey = base64Decoded().toPublicKey()


/**
 * Accepts a base64 encoded string and returns
 * a ECDSA [PrivateKey] via an [X509EncodedKeySpec].
 */
fun String.toPrivateKey(): PrivateKey = base64Decoded().toPrivateKey()

fun Key.base64Encoded(): String = encoded.base64Encoded()

fun Key.toHexString(): String = encoded.toHexString()