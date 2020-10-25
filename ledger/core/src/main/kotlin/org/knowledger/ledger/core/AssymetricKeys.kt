package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.crypto.EncodedPrivateKey
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.toPrivateKey
import org.knowledger.ledger.crypto.toPublicKey

@OptIn(ExperimentalSerializationApi::class)
fun EncodedPrivateKey.generateSignature(
    data: HashSerializable, encoder: BinaryFormat,
): EncodedSignature = toPrivateKey().generateSignature(data, encoder)

/**
 * Verifies a [String] signature.
 */
fun EncodedSignature.verifyECDSASig(publicKey: EncodedPublicKey, data: String): Boolean =
    verifyECDSASig(publicKey, data.toByteArray())


/**
 * Verifies a [ByteArray] signature.
 */
fun EncodedSignature.verifyECDSASig(publicKey: EncodedPublicKey, data: ByteArray): Boolean =
    with(dsa) {
        initVerify(publicKey.toPublicKey())
        update(data)
        verify(bytes)
    }