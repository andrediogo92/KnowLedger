package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.LedgerContract

data class TransactionWithBlockHash(
    val txBlockHash: Hash, val txHash: Hash,
    val txIndex: Int, val txMillis: Long,
    val txMin: Long, val txData: PhysicalData
) : LedgerContract