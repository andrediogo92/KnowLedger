package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat

interface SignedTransaction : Transaction {
    val signature: ByteArray

    /**
     * Verifies the value we signed hasn't been
     * tampered with.
     *
     * @return Whether the value was signed with the
     * corresponding private key.
     */
    fun verifySignature(encoder: BinaryFormat): Boolean

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(encoder: BinaryFormat): Boolean
}