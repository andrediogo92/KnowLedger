package org.knowledger.ledger.storage.transaction.output.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.service.CloningFactory
import org.knowledger.ledger.storage.TransactionOutput

internal interface TransactionOutputFactory :
    CloningFactory<TransactionOutput> {
    fun create(
        payout: Payout, prevTxBlock: Hash,
        prevTxIndex: Int, prevTx: Hash,
        txIndex: Int, tx: Hash
    ): TransactionOutput
}