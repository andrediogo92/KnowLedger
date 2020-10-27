package org.knowledger.ledger.chain.service

import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.TransactionReference
import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.chain.data.WitnessInfo
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableTransaction

internal interface WitnessService {
    /**
     * Calculate new Witness data when there is no known witness
     * in this coinbase and no previous transaction.
     */
    fun calculateNewWitnessWithoutTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, witnessInfo: WitnessInfo?, chainInfo: ChainInfo,
    ): WitnessReference

    /**
     * Calculate new Witness data when there is no witness
     * already present in this block.
     *
     * Last known Witness is in [witnessInfo].
     *
     * There is a known transaction to compare the [newTransaction]
     * in [lastTransaction].
     */
    fun calculateNewWitnessWithTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, lastTransaction: TransactionWithBlockHash,
        witnessInfo: WitnessInfo?, chainInfo: ChainInfo,
    ): WitnessReference

    /**
     * Calculate the Witness data when there is a witness
     * already present by [witnessReference] in this block.
     *
     * There is a known transaction to compare the [newTransaction]
     * in [lastTransaction].
     */
    fun calculateWithWitnessAndTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, lastTransaction: TransactionWithBlockHash,
        witnessReference: WitnessReference, chainInfo: ChainInfo,
    ): WitnessReference

    /**
     * Calculate the Witness data when there is a witness
     * already present by [witnessReference] in this block.
     *
     * There is no known transaction to compare to the [newTransaction].
     */
    fun calculateWithWitnessAndNoTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, witnessReference: WitnessReference,
        chainInfo: ChainInfo,
    ): WitnessReference
}