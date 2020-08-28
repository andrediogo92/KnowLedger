package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.verifyECDSASig
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.storage.serial.TransactionSerializationStrategy

@OptIn(ExperimentalSerializationApi::class)
interface SignedTransaction : Transaction {
    val signature: EncodedSignature

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(TransactionSerializationStrategy, this)

    /**
     * Verifies the value we signed hasn't been
     * tampered with.
     *
     * @return Whether the value was signed with the
     * corresponding private key.
     */
    fun verifySignature(encoder: BinaryFormat): Boolean =
        signature.verifyECDSASig(publicKey, serialize(encoder))

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(encoder: BinaryFormat): Boolean
}