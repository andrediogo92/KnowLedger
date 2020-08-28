package org.knowledger.ledger.core

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.crypto.EncodedSignature
import java.security.PrivateKey
import java.security.Signature

internal val dsa: Signature = Signature.getInstance("ECDSA", "BC")


/**
 * Signs the [data]'s byte encoding using the [encoder].
 * Returns the generated signature as a [ByteArray].
 */
@OptIn(ExperimentalSerializationApi::class)
fun PrivateKey.generateSignature(data: HashSerializable, encoder: BinaryFormat): EncodedSignature =
    applyECDSASig(data.serialize(encoder))


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun PrivateKey.applyECDSASig(input: String): EncodedSignature =
    applyECDSASig(input.toByteArray())


/**
 * Applies ECDSA Signature and returns the result (as [ByteArray]).
 */
fun PrivateKey.applyECDSASig(input: ByteArray): EncodedSignature =
    with(dsa) {
        initSign(this@applyECDSASig)
        update(input)
        EncodedSignature(sign())
    }

