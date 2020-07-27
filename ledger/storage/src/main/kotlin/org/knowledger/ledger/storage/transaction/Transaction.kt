package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.serial.TransactionSerializationStrategy
import java.security.PublicKey

interface Transaction : HashSerializable, LedgerContract,
                        Comparable<Transaction> {
    // Agent's pub key.
    val publicKey: PublicKey
    val data: PhysicalData

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(TransactionSerializationStrategy, this)

    override fun compareTo(other: Transaction): Int =
        data.compareTo(other.data)
}