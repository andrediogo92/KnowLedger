package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.serial.HashedTransactionSerializer

@Serializable(with = HashedTransactionSerializer::class)
interface HashedTransaction : Hashing,
                              SignedTransaction