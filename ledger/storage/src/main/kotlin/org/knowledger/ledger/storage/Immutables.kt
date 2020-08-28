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

fun Block.immutableCopy(): ImmutableBlock = ImmutableBlock(
    blockHeader.immutableCopy(), coinbase.immutableCopy(), merkleTree.immutableCopy(),
    transactions.map(Transaction::immutableCopy).toMutableSortedListFromPreSorted(),
    approximateSize
)

fun BlockHeader.immutableCopy(): ImmutableBlockHeader = ImmutableBlockHeader(
    chainHash, hash, merkleRoot, previousHash, blockParams.immutableCopy(), seconds, nonce
)

fun BlockParams.immutableCopy(): ImmutableBlockParams =
    ImmutableBlockParams(blockLength, blockMemorySize)

fun ChainId.immutableCopy(): ImmutableChainId =
    ImmutableChainId(hash, ledgerHash, tag, blockParams, coinbaseParams)

fun Coinbase.immutableCopy(): ImmutableCoinbase = ImmutableCoinbase(
    coinbaseHeader.immutableCopy(), merkleTree.immutableCopy(),
    witnesses.map(Witness::immutableCopy).toMutableSortedListFromPreSorted(),
)

fun CoinbaseParams.immutableCopy(): ImmutableCoinbaseParams = ImmutableCoinbaseParams(
    hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold, formula
)

fun CoinbaseHeader.immutableCopy(): ImmutableCoinbaseHeader = ImmutableCoinbaseHeader(
    hash, merkleRoot, payout, blockheight, difficulty,
    extraNonce, coinbaseParams.immutableCopy(),
)

fun LedgerParams.immutableCopy(): ImmutableLedgerParams =
    ImmutableLedgerParams(hashers, recalculationTime, recalculationTrigger)

fun Transaction.immutableCopy(): ImmutableTransaction =
    ImmutableTransaction(hash, signature, publicKey, data, approximateSize)

fun TransactionOutput.immutableCopy(): ImmutableTransactionOutput =
    ImmutableTransactionOutput(payout, prevTxBlock, prevTxIndex, prevTx, txIndex, tx)

fun Witness.immutableCopy(): ImmutableWitness = ImmutableWitness(
    hash, publicKey, previousWitnessIndex, previousCoinbase, payout,
    transactionOutputs.map(TransactionOutput::immutableCopy).toMutableSortedListFromPreSorted()
)