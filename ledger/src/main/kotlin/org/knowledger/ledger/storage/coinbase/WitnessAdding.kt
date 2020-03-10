package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal interface WitnessAdding {
    fun addToWitness(
        witness: Witness,
        newIndex: Int, newTransaction: Transaction,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash
    )

    fun addToWitness(
        newIndex: Int,
        newTransaction: Transaction,
        previousWitnessIndex: Int,
        latestCoinbase: Hash,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter? = null
    )
}