package org.knowledger.ledger.crypto

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

private val keyFactory: KeyFactory =
    KeyFactory.getInstance(
        "ECDSA",
        "BC"
    )

internal fun ByteArray.toPublicKey(): PublicKey =
    keyFactory.generatePublic(
        X509EncodedKeySpec(this)
    )

internal fun ByteArray.toPrivateKey(): PrivateKey =
    keyFactory.generatePrivate(
        PKCS8EncodedKeySpec(this)
    )


/**
 * Accepts a byte encoded [EncodedPublicKey]
 * and returns the resulting ECDSA [PublicKey] via
 * a [X509EncodedKeySpec].
 */
fun EncodedPublicKey.toPublicKey(): PublicKey =
    bytes.toPublicKey()


/**
 * Accepts a byte encoded [EncodedPrivateKey]
 * and returns the resulting ECDSA [PrivateKey]
 * via a [PKCS8EncodedKeySpec].
 */
fun EncodedPrivateKey.toPrivateKey(): PrivateKey =
    bytes.toPrivateKey()
