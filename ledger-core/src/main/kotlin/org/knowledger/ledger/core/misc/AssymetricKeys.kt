package org.knowledger.ledger.core.misc

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.serial.HashSerializable
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

private val dsa: Signature =
    Signature.getInstance(
        "ECDSA",
        "BC"
    )

/**
 * Signs the [data]'s byte encoding using the [privateKey].
 * Returns the generated signature as a [ByteArray].
 */
fun generateSignature(
    privateKey: PrivateKey,
    data: HashSerializable,
    encoder: BinaryFormat
): ByteArray =
    applyECDSASig(
        privateKey,
        data.serialize(encoder)
    )


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun applyECDSASig(
    privateKey: PrivateKey,
    input: String
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
): ByteArray =
    with(dsa) {
        initSign(privateKey)
        update(input)
        sign()
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
): Boolean =
    with(dsa) {
        initVerify(publicKey)
        update(data)
        verify(signature)
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