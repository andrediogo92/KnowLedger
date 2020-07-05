package org.knowledger.ledger.storage.transaction.output.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput
import org.knowledger.ledger.storage.transaction.output.TransactionOutput

internal object TransactionOutputFactoryImpl : TransactionOutputFactory {
    override fun create(
        payout: Payout, prevTxBlock: Hash,
        prevTxIndex: Int, prevTx: Hash,
        txIndex: Int, tx: Hash
    ): ImmutableTransactionOutput =
        ImmutableTransactionOutput(
            payout, prevTxBlock,
            prevTxIndex, prevTx,
            txIndex, tx
        )

    override fun create(
        other: TransactionOutput
    ): ImmutableTransactionOutput = ImmutableTransactionOutput(
        payout = other.payout,
        prevTxBlock = other.prevTxBlock,
        prevTxIndex = other.prevTxIndex,
        prevTx = other.prevTx,
        txIndex = other.txIndex,
        tx = other.tx
    )
}