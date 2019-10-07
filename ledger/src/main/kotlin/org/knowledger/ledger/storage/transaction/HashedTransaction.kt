package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.serial.TransactionSerializer

@Serializable(with = TransactionSerializer::class)
interface HashedTransaction : Hashing,
                              SignedTransaction {
    override fun clone(): HashedTransaction
}