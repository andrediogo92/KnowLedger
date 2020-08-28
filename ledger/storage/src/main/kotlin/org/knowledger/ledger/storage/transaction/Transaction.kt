package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.serial.TransactionSerializationStrategy

interface Transaction : HashSerializable, LedgerContract, Comparable<Transaction> {
    // Agent's pub key.
    val publicKey: EncodedPublicKey
    val data: PhysicalData

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(TransactionSerializationStrategy, this)

    override fun compareTo(other: Transaction): Int = data.compareTo(other.data)
}