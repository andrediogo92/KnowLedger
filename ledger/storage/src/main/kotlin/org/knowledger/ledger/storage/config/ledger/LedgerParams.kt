package org.knowledger.ledger.storage.config.ledger

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerContract

interface LedgerParams : LedgerContract {
    val hashers: Hash
    val recalculationTime: Long
    val recalculationTrigger: Int
}