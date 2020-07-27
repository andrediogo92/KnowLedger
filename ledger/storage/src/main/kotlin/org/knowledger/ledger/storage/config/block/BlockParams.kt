package org.knowledger.ledger.storage.config.block

import org.knowledger.ledger.storage.HashSerializable
import org.knowledger.ledger.storage.LedgerContract

interface BlockParams : HashSerializable, LedgerContract {
    val blockMemorySize: Int
    val blockLength: Int
}