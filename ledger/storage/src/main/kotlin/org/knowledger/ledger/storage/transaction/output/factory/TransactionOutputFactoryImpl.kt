package org.knowledger.ledger.storage.transaction.output.factory

import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput

internal class TransactionOutputFactoryImpl : TransactionOutputFactory {
    override fun create(
        payout: Payout, prevTxBlock: Hash, prevTxIndex: Int, prevTx: Hash, txIndex: Int, tx: Hash,
    ): ImmutableTransactionOutput =
        ImmutableTransactionOutput(payout, prevTxBlock, prevTxIndex, prevTx, txIndex, tx)

    override fun create(other: TransactionOutput): ImmutableTransactionOutput =
        with(other) { create(payout, prevTxBlock, prevTxIndex, prevTx, txIndex, tx) }
}