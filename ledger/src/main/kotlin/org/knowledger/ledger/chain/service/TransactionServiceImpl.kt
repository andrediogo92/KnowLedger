package org.knowledger.ledger.chain.service

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.map
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.chain.ChainInfo
import org.knowledger.ledger.chain.data.TransactionReference
import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.chain.data.WitnessReference
import org.knowledger.ledger.chain.transactions.QueryManager
import org.knowledger.ledger.chain.transactions.getTransactionByBound
import org.knowledger.ledger.chain.transactions.getWitnessInfoBy
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.results.LoadFailure
import kotlin.math.abs

internal class TransactionServiceImpl() : TransactionService {

    override fun calculateTransactionDifference(
        block: MutableBlock, newTransaction: MutableTransaction,
        queryManager: QueryManager, chainInfo: ChainInfo, witnessService: WitnessService,
    ): Outcome<WitnessReference, LoadFailure> {
        val previousTransaction = block.transactions
            .minByOrNull { abs(it.data.millis - newTransaction.data.millis) }
            ?.let { transaction ->
                TransactionWithBlockHash(
                    block.blockHeader.hash, transaction.hash,
                    block.transactions.binarySearch(transaction),
                    transaction.data.millis, transaction.data.millis, transaction.data,
                )
            }


        return if (block + newTransaction) {
            val nearest =
                calculateNearestTransaction(previousTransaction, newTransaction, queryManager)
            val witnessReference =
                WitnessReference(block.coinbase.findWitness(newTransaction.publicKey))
            val transactionReference =
                TransactionReference(block.transactions.binarySearch(newTransaction))

            val result = nearest.flatMap { withBlockHash ->
                calculateWitnessWithExistingTransaction(
                    block.coinbase, witnessReference, withBlockHash, transactionReference,
                    newTransaction, queryManager, chainInfo, witnessService,
                )
            }
            when (result) {
                is Ok<WitnessReference> -> result
                is Err<LoadFailure> -> when (result.error) {
                    is LoadFailure.NonExistentData ->
                        calculateWitnessWithNoExistingTransaction(
                            block.coinbase, witnessReference, transactionReference,
                            newTransaction, queryManager, chainInfo, witnessService,
                        )
                    else -> result
                }
            }

        } else {
            LoadFailure.DuplicatedTransaction(
                "Transaction with hash ${newTransaction.hash.base64Encoded()} already exists").err()
        }

    }

    private fun calculateNearestTransaction(
        previousTransaction: TransactionWithBlockHash?, newTransaction: Transaction,
        queryManager: QueryManager,
    ): Outcome<TransactionWithBlockHash, LoadFailure> {
        val diff = if (previousTransaction != null) {
            abs(newTransaction.data.millis - previousTransaction.txMillis)
        } else {
            abs(newTransaction.data.millis - 1)
        }
        val dbResult =
            queryManager.getTransactionByBound(newTransaction.data.millis, diff)
        return when (dbResult) {
            is Ok -> dbResult
            is Err -> when (dbResult.error) {
                is LoadFailure.NonExistentData -> previousTransaction?.ok() ?: dbResult
                else -> dbResult
            }
        }
    }

    private fun calculateWitnessWithNoExistingTransaction(
        coinbase: MutableCoinbase, witnessReference: WitnessReference,
        txReference: TransactionReference, newTransaction: MutableTransaction,
        queryManager: QueryManager, chainInfo: ChainInfo, witnessService: WitnessService,
    ): Outcome<WitnessReference, LoadFailure> =
        if (witnessReference.index > 0) {
            witnessService.calculateWithWitnessAndNoTransaction(
                coinbase, txReference, newTransaction, witnessReference, chainInfo,
            ).ok()
        } else {
            val result = queryManager.getWitnessInfoBy(newTransaction.publicKey)
                .map { info ->
                    witnessService.calculateNewWitnessWithoutTransaction(
                        coinbase, txReference, newTransaction, info, chainInfo,
                    )
                }
            when (result) {
                is Ok -> result
                is Err -> when (result.error) {
                    is LoadFailure.NonExistentData ->
                        witnessService.calculateNewWitnessWithoutTransaction(
                            coinbase, txReference, newTransaction, null, chainInfo,
                        ).ok()
                    else -> result
                }
            }
        }

    private fun calculateWitnessWithExistingTransaction(
        coinbase: MutableCoinbase, witnessReference: WitnessReference,
        withBlockHash: TransactionWithBlockHash,
        txReference: TransactionReference, newTransaction: MutableTransaction,
        queryManager: QueryManager, chainInfo: ChainInfo, witnessService: WitnessService,
    ): Outcome<WitnessReference, LoadFailure> =
        if (witnessReference.index > 0) {
            witnessService.calculateWithWitnessAndTransaction(
                coinbase, txReference, newTransaction, withBlockHash, witnessReference, chainInfo,
            ).ok()
        } else {
            val result = queryManager
                .getWitnessInfoBy(newTransaction.publicKey)
                .map { info ->
                    witnessService.calculateNewWitnessWithTransaction(
                        coinbase, txReference, newTransaction, withBlockHash, info, chainInfo,
                    )
                }
            when (result) {
                is Ok -> result
                is Err -> when (result.error) {
                    is LoadFailure.NonExistentData ->
                        witnessService.calculateNewWitnessWithTransaction(
                            coinbase, txReference, newTransaction, withBlockHash, null, chainInfo,
                        ).ok()
                    else -> result
                }
            }
        }
}