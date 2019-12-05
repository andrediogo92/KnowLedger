package org.knowledger.ledger.crypto.hash

import org.knowledger.ledger.core.base.hash.toHexString
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import java.security.PrivateKey
import java.security.PublicKey

fun EncodedSignature.toHexString(): String =
    encoded.toHexString()

fun EncodedPrivateKey.toHexString(): String =
    encoded.toHexString()

fun EncodedPublicKey.toHexString(): String =
    encoded.toHexString()

fun PublicKey.toEncoded(): EncodedPublicKey =
    EncodedPublicKey(encoded)

fun PrivateKey.toEncoded(): EncodedPrivateKey =
    EncodedPrivateKey(encoded)
