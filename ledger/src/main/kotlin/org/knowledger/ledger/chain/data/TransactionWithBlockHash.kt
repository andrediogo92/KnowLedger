package org.knowledger.ledger.chain.data

import org.knowledger.ledger.chain.ServiceClass
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.PhysicalData

data class TransactionWithBlockHash(
    val txBlockHash: Hash, val txHash: Hash, val txIndex: Int,
    val txMillis: Long, val txMin: Long, val txData: PhysicalData,
) : ServiceClass