package org.knowledger.ledger.storage.witness

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.mutations.HashUpdateable
import org.knowledger.ledger.storage.mutations.Indexed

interface MutableHashedWitness : HashedWitness, HashUpdateable, Indexed {
    val mutableTransactionOutputs: MutableSortedList<TransactionOutput>

    fun addToPayout(transactionOutput: TransactionOutput)
}