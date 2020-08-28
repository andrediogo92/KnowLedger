package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.core.data.Sizeable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.storage.serial.SignedTransactionSerializationStrategy

interface HashedTransaction : Hashing, Sizeable,
                              SignedTransaction {
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(SignedTransactionSerializationStrategy, this)
}