package org.knowledger.ledger.crypto

import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Security
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

private val index = if (Security.getProvider("BC") == null) {
    Security.addProvider(BouncyCastleProvider())
} else {
    -1
}

internal val keyFactory: KeyFactory =
    KeyFactory.getInstance("ECDSA", "BC")

internal val keygen = KeyPairGenerator.getInstance("ECDSA", "BC")

internal val random = try {
    SecureRandom.getInstance("NativePRNGNonBlocking")
} catch (e: Exception) {
    SecureRandom.getInstance("SHA1PRNG")
}

internal val ecSpec = ECGenParameterSpec("P-521")

internal fun ByteArray.toPublicKey(): PublicKey =
    keyFactory.generatePublic(X509EncodedKeySpec(this))

internal fun ByteArray.toPrivateKey(): PrivateKey =
    keyFactory.generatePrivate(PKCS8EncodedKeySpec(this))

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

fun EncodedKeyPair.toKeyPair(): KeyPair =
    KeyPair(publicKey.toPublicKey(), privateKey.toPrivateKey())


fun PublicKey.toEncoded(): EncodedPublicKey =
    EncodedPublicKey(encoded)

fun PrivateKey.toEncoded(): EncodedPrivateKey =
    EncodedPrivateKey(encoded)

internal fun KeyPair.toEncoded(): EncodedKeyPair =
    EncodedKeyPair(public.toEncoded(), private.toEncoded())
