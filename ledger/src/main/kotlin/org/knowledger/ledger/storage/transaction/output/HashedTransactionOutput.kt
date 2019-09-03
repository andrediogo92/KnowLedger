package org.knowledger.ledger.storage.transaction.output

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.serial.HashedTransactionOutputSerializer

@Serializable(with = HashedTransactionOutputSerializer::class)
interface HashedTransactionOutput : Hashing,
                                    TransactionOutput