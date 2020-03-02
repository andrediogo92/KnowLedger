package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout

fun transactionOutput(
    payout: Payout,
    newIndex: Int,
    newTransaction: Hash,
    previousBlock: Hash,
    previousIndex: Int,
    previousTransaction: Hash
): TransactionOutput =
    TransactionOutputImpl(
        payout = payout,
        prevTxBlock = previousBlock,
        prevTxIndex = previousIndex,
        prevTx = previousTransaction,
        txIndex = newIndex, tx = newTransaction
    )
