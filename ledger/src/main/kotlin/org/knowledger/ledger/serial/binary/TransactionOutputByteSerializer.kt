package org.knowledger.ledger.serial.binary

import org.knowledger.ledger.serial.internal.AbstractTransactionOutputSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes
import org.knowledger.ledger.storage.TransactionOutput

/**
 * A pretty printing friendly serializer for [TransactionOutput].
 * It encodes all byte data directly as ByteArray.
 */
internal object TransactionOutputByteSerializer : AbstractTransactionOutputSerializer(),
                                                  HashEncodeInBytes