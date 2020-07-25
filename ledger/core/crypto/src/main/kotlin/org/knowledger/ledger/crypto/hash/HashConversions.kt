package org.knowledger.ledger.crypto.hash

import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import java.security.PrivateKey
import java.security.PublicKey

fun PublicKey.toEncoded(): EncodedPublicKey =
    EncodedPublicKey(encoded)

fun PrivateKey.toEncoded(): EncodedPrivateKey =
    EncodedPrivateKey(encoded)