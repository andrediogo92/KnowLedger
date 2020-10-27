package org.knowledger.ledger.chain.service

import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.TransactionHashes
import org.knowledger.ledger.chain.data.TransactionReference
import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.chain.data.WitnessInfo
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.DataFormula
import org.knowledger.ledger.storage.Factories
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.Payout

internal class WitnessServiceImpl : WitnessService {
    private fun TransactionWithBlockHash.toTransactionHashes(): TransactionHashes =
        TransactionHashes(txBlockHash, txHash, txIndex)

    private fun emptyTransactionHashes(): TransactionHashes =
        TransactionHashes(Hash.emptyHash, Hash.emptyHash, -1)


    override fun calculateNewWitnessWithoutTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, witnessInfo: WitnessInfo?, chainInfo: ChainInfo,
    ): WitnessReference {
        val payoutToAdd = calculatePayout(
            newTransaction, chainInfo.coinbaseParams, chainInfo.formula,
        )
        coinbase.coinbaseHeader.addToPayout(payoutToAdd)

        val index = calculateWitness(
            coinbase, txReference, newTransaction, emptyTransactionHashes(),
            payoutToAdd, witnessInfo, chainInfo,
        )
        return WitnessReference(index)
    }

    override fun calculateNewWitnessWithTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, lastTransaction: TransactionWithBlockHash,
        witnessInfo: WitnessInfo?, chainInfo: ChainInfo,
    ): WitnessReference {
        val payoutToAdd = calculatePayout(
            newTransaction, lastTransaction, chainInfo.coinbaseParams, chainInfo.formula,
        )
        coinbase.coinbaseHeader.addToPayout(payoutToAdd)

        val index = calculateWitness(
            coinbase, txReference, newTransaction, lastTransaction.toTransactionHashes(),
            payoutToAdd, witnessInfo, chainInfo
        )
        return WitnessReference(index)
    }

    override fun calculateWithWitnessAndTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, lastTransaction: TransactionWithBlockHash,
        witnessReference: WitnessReference, chainInfo: ChainInfo,
    ): WitnessReference {
        val payoutToAdd = calculatePayout(
            newTransaction, lastTransaction, chainInfo.coinbaseParams, chainInfo.formula,
        )
        coinbase.coinbaseHeader.addToPayout(payoutToAdd)

        calculateWitness(
            coinbase, txReference, newTransaction, lastTransaction.toTransactionHashes(),
            payoutToAdd, witnessReference, chainInfo.factories,
        )
        return witnessReference
    }

    override fun calculateWithWitnessAndNoTransaction(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, witnessReference: WitnessReference,
        chainInfo: ChainInfo,
    ): WitnessReference {
        val payoutToAdd = calculatePayout(
            newTransaction, chainInfo.coinbaseParams, chainInfo.formula,
        )
        coinbase.coinbaseHeader.addToPayout(payoutToAdd)

        calculateWitness(
            coinbase, txReference, newTransaction, emptyTransactionHashes(),
            payoutToAdd, witnessReference, chainInfo.factories,
        )
        return witnessReference
    }

    private fun calculateWitness(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, transactionHashes: TransactionHashes,
        payout: Payout, witnessReference: WitnessReference, factories: Factories,
    ) {
        val witness = coinbase.mutableWitnesses[witnessReference.index]
        witness.addToPayout(
            factories.transactionOutputFactory.create(
                payout, transactionHashes.blockHash, transactionHashes.txIndex,
                transactionHashes.txHash, txReference.index, newTransaction.hash,
            )
        )
    }

    private fun calculateWitness(
        coinbase: MutableCoinbase, txReference: TransactionReference,
        newTransaction: MutableTransaction, transactionHashes: TransactionHashes,
        payout: Payout, witnessInfo: WitnessInfo?, chainInfo: ChainInfo,
    ): Int {
        val witness = chainInfo.factories.witnessFactory.create(
            newTransaction.publicKey, witnessInfo?.index ?: -1,
            witnessInfo?.hash ?: Hash.emptyHash,
            chainInfo.factories.transactionOutputFactory.create(
                payout, transactionHashes.blockHash, transactionHashes.txIndex,
                transactionHashes.txHash, txReference.index, newTransaction.hash,
            ),
            chainInfo.hashers, chainInfo.encoder,
        )
        coinbase.addToOutputs(witness)
        return coinbase.mutableWitnesses.binarySearch(witness)
    }


    private fun calculatePayout(
        newTransaction: MutableTransaction, lastTransaction: TransactionWithBlockHash,
        coinbaseParams: CoinbaseParams, formula: DataFormula,
    ): Payout = coinbaseParams.calculatePayout(
        newTransaction.data, lastTransaction.txData, formula,
    )

    private fun calculatePayout(
        newTransaction: MutableTransaction, coinbaseParams: CoinbaseParams, formula: DataFormula,
    ): Payout = coinbaseParams.calculatePayout(newTransaction.data, formula)
}