package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.Serializable
import org.knowledger.ledger.crypto.hash.Hashing
import org.knowledger.ledger.serial.TransactionOutputSerializer

@Serializable(with = TransactionOutputSerializer::class)
interface HashedTransactionOutput : Hashing,
                                    TransactionOutput {
    override fun clone(): HashedTransactionOutput
}