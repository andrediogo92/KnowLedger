package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.storage.TransactionOutput

internal interface PayoutAdding {
    fun addToPayout(
        transactionOutput: TransactionOutput
    )
}