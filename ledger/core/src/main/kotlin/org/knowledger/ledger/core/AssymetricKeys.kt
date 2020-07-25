package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.data.hash.toHexString
import org.knowledger.ledger.crypto.EncodedSignature
import java.security.Key
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature

private val dsa: Signature =
    Signature.getInstance(
        "ECDSA",
        "BC"
    )

/**
 * Signs the [data]'s byte encoding using the [encoder].
 * Returns the generated signature as a [ByteArray].
 */
fun PrivateKey.generateSignature(
    data: HashSerializable,
    encoder: BinaryFormat
): EncodedSignature =
    applyECDSASig(
        data.serialize(encoder)
    )


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun PrivateKey.applyECDSASig(
    input: String
): EncodedSignature =
    applyECDSASig(
        input.toByteArray()
    )


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun PrivateKey.applyECDSASig(
    input: ByteArray
): EncodedSignature =
    with(dsa) {
        initSign(this@applyECDSASig)
        update(input)
        EncodedSignature(sign())
    }

/**
 * Verifies a [String] signature.
 */
fun EncodedSignature.verifyECDSASig(
    publicKey: PublicKey,
    data: String
): Boolean =
    this.verifyECDSASig(
        publicKey,
        data.toByteArray()
    )


/**
 * Verifies a [ByteArray] signature.
 */
fun EncodedSignature.verifyECDSASig(
    publicKey: PublicKey,
    data: ByteArray
): Boolean =
    with(dsa) {
        initVerify(publicKey)
        update(data)
        verify(bytes)
    }

fun Key.toHexString(): String = encoded.toHexString()

fun Key.truncatedHexString(size: Int = Hash.TRUNC): String =
    if (encoded.size > size) {
        encoded.sliceArray(0 until size).toHexString()
    } else {
        encoded.toHexString()
    }
