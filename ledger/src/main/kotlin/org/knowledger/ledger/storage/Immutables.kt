package org.knowledger.ledger.storage

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ImmutableChainId
import org.knowledger.ledger.crypto.storage.immutableCopy
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput
import org.knowledger.ledger.storage.witness.ImmutableWitness

fun Block.immutableCopy(): ImmutableBlock =
    ImmutableBlock(
        immutableTransactions = transactions
            .map { it.immutableCopy() }
            .toMutableSortedListFromPreSorted(),
        coinbase = coinbase.immutableCopy(),
        header = header.immutableCopy(),
        merkleTree = merkleTree.immutableCopy(),
        approximateSize = approximateSize
    )

fun BlockHeader.immutableCopy(): ImmutableBlockHeader =
    ImmutableBlockHeader(
        chainId = chainId.immutableCopy(), merkleRoot = merkleRoot,
        previousHash = previousHash, params = params,
        seconds = seconds, nonce = nonce, hash = hash
    )

fun ChainId.immutableCopy(): ImmutableChainId =
    ImmutableChainId(tag, ledgerHash, hash)

fun Coinbase.immutableCopy(): ImmutableCoinbase =
    ImmutableCoinbase(
        header = header.immutableCopy(),
        immutableWitnesses = witnesses
            .map { it.immutableCopy() }
            .toMutableSortedListFromPreSorted(),
        merkleTree = merkleTree.immutableCopy()
    )

fun CoinbaseHeader.immutableCopy(): ImmutableCoinbaseHeader =
    ImmutableCoinbaseHeader(
        hash = hash, payout = payout, coinbaseParams = coinbaseParams,
        merkleRoot = merkleRoot, difficulty = difficulty,
        blockheight = blockheight, extraNonce = extraNonce
    )

fun Transaction.immutableCopy(): ImmutableTransaction =
    ImmutableTransaction(
        hash = hash, signature = signature,
        publicKey = publicKey, data = data
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