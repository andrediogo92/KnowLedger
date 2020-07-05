package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.SignedTransactionSerializationStrategy

interface HashedTransaction : Hashing, Sizeable,
                              SignedTransaction {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(SignedTransactionSerializationStrategy, this)
}