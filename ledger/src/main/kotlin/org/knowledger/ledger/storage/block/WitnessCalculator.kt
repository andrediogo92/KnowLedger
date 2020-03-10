package org.knowledger.ledger.storage.block

import org.knowledger.ledger.core.base.hash.Hash.Companion.emptyHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.transactions.TransactionWithBlockHash
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

internal interface WitnessCalculator {
    /**
     * Calculate new Witness data when there is no witness
     * already present in this block.
     *
     * There is a known transaction to compare the [newTransaction]
     * in [lastTransaction].
     */
    fun calculateWitness(
        newTransaction: Transaction, lastTransaction: TransactionWithBlockHash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int = -1, coinbaseHash: Hash = emptyHash
    )

    /**
     * Calculate the Witness data when there is a witness
     * already present at [witnessIndex] in this block.
     *
     * There is a known transaction to compare the [newTransaction]
     * in [lastTransaction].
     */
    fun calculateWitness(
        witnessIndex: Int, newTransaction: Transaction,
        lastTransaction: TransactionWithBlockHash
    )

    /**
     * Calculate new Witness data when there is no known witness
     * in this coinbase and reference a possible previous
     * witness by [previousWitnessIndex].
     *
     * There may be a earlier transaction to reference in its
     * expanded form, with all [latestKnown], [latestKnownBlockHash],
     * [latestKnownHash] and [latestKnownBlockHash] parameters.
     */
    fun calculateWitness(
        newTransaction: Transaction,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int = -1,
        coinbaseHash: Hash = emptyHash,
        latestKnownIndex: Int = -1, latestKnownHash: Hash = emptyHash,
        latestKnown: PhysicalData? = null,
        latestKnownBlockHash: Hash = emptyHash
    )

    /**
     * Calculate the Witness data when there is a witness
     * already present at [witnessIndex] in this block.
     *
     * There may be a earlier transaction to reference in its
     * expanded form, with all [latestKnown], [latestKnownBlockHash],
     * [latestKnownHash] and [latestKnownBlockHash] parameters.
     */
    fun calculateWitness(
        newTransaction: Transaction, witnessIndex: Int,
        latestKnownIndex: Int = -1, latestKnownHash: Hash = emptyHash,
        latestKnown: PhysicalData? = null,
        latestKnownBlockHash: Hash = emptyHash
    )
}