package org.knowledger.ledger.serial.display

import org.knowledger.ledger.serial.internal.AbstractTransactionOutputSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay
import org.knowledger.ledger.storage.TransactionOutput

/**
 * A pretty printing friendly serializer for [TransactionOutput].
 * It encodes all byte data base64 encoded.
 */
internal object TransactionOutputSerializer : AbstractTransactionOutputSerializer(),
                                              HashEncodeForDisplay