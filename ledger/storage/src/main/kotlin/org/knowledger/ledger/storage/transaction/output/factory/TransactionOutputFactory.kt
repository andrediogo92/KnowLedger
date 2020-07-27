package org.knowledger.ledger.storage.transaction.output.factory

import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.transaction.output.TransactionOutput

interface TransactionOutputFactory : CloningFactory<TransactionOutput> {
    fun create(
        payout: Payout, prevTxBlock: Hash,
        prevTxIndex: Int, prevTx: Hash,
        txIndex: Int, tx: Hash
    ): TransactionOutput
}