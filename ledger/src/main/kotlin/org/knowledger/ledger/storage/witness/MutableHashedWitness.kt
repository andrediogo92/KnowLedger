package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.Indexed
import org.knowledger.ledger.storage.TransactionOutput

internal interface MutableHashedWitness : HashedWitness, HashUpdateable,
                                          Indexed {
    fun addToPayout(
        transactionOutput: TransactionOutput
    )
}