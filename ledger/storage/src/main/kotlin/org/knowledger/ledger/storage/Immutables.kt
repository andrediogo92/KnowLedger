package org.knowledger.ledger.storage

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.storage.immutableCopy
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.config.block.ImmutableBlockParams
import org.knowledger.ledger.storage.config.chainid.ImmutableChainId
import org.knowledger.ledger.storage.config.coinbase.ImmutableCoinbaseParams
import org.knowledger.ledger.storage.config.ledger.ImmutableLedgerParams
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput
import org.knowledger.ledger.storage.witness.ImmutableWitness

fun Block.immutableCopy(): ImmutableBlock =
    ImmutableBlock(
        immutableTransactions = transactions
            .map { it.immutableCopy() }
            .toMutableSortedListFromPreSorted(),
        coinbase = coinbase.immutableCopy(),
        blockHeader = blockHeader.immutableCopy(),
        merkleTree = merkleTree.immutableCopy(),
        approximateSize = approximateSize
    )

fun BlockHeader.immutableCopy(): ImmutableBlockHeader =
    ImmutableBlockHeader(
        chainHash = chainHash, merkleRoot = merkleRoot,
        previousHash = previousHash, blockParams = blockParams.immutableCopy(),
        seconds = seconds, nonce = nonce, hash = hash
    )

fun BlockParams.immutableCopy(): ImmutableBlockParams =
    ImmutableBlockParams(
        blockLength = blockLength, blockMemorySize = blockMemorySize
    )

fun ChainId.immutableCopy(): ImmutableChainId =
    ImmutableChainId(hash, ledgerHash, tag, blockParams, coinbaseParams)

fun Coinbase.immutableCopy(): ImmutableCoinbase =
    ImmutableCoinbase(
        coinbaseHeader = coinbaseHeader.immutableCopy(),
        immutableWitnesses = witnesses
            .map { it.immutableCopy() }
            .toMutableSortedListFromPreSorted(),
        merkleTree = merkleTree.immutableCopy()
    )

fun CoinbaseParams.immutableCopy(): ImmutableCoinbaseParams =
    ImmutableCoinbaseParams(
        hashSize = hashSize, timeIncentive = timeIncentive,
        valueIncentive = valueIncentive, baseIncentive = baseIncentive,
        dividingThreshold = dividingThreshold, formula = formula
    )

fun CoinbaseHeader.immutableCopy(): ImmutableCoinbaseHeader =
    ImmutableCoinbaseHeader(
        hash = hash, payout = payout,
        coinbaseParams = coinbaseParams.immutableCopy(),
        merkleRoot = merkleRoot, difficulty = difficulty,
        blockheight = blockheight, extraNonce = extraNonce
    )

fun LedgerParams.immutableCopy(): ImmutableLedgerParams =
    ImmutableLedgerParams(
        hashers = hashers, recalculationTime = recalculationTime,
        recalculationTrigger = recalculationTrigger
    )

fun Transaction.immutableCopy(): ImmutableTransaction =
    ImmutableTransaction(
        hash = hash, signature = signature,
        publicKey = publicKey, data = data,
        approximateSize = approximateSize
    )

fun TransactionOutput.immutableCopy(): ImmutableTransactionOutput =
    ImmutableTransactionOutput(
        payout = payout, prevTxBlock = prevTxBlock,
        prevTxIndex = prevTxIndex, prevTx = prevTx,
        txIndex = txIndex, tx = tx
    )

fun Witness.immutableCopy(): ImmutableWitness =
    ImmutableWitness(
        hash = hash, publicKey = publicKey,
        previousWitnessIndex = previousWitnessIndex,
        previousCoinbase = previousCoinbase, payout = payout,
        immutableTransactionOutputs = transactionOutputs
            .map { it.immutableCopy() }
            .toMutableSortedListFromPreSorted()
    )