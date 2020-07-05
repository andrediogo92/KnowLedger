package org.knowledger.ledger.storage.transaction.output.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.StorageAwareTransactionOutputImpl

internal class StorageAwareTransactionOutputFactory(
    private val factory: TransactionOutputFactory = TransactionOutputFactoryImpl
) : TransactionOutputFactory {
    private fun createSA(
        transactionOutput: TransactionOutput
    ): StorageAwareTransactionOutputImpl =
        StorageAwareTransactionOutputImpl(transactionOutput)

    override fun create(
        payout: Payout, prevTxBlock: Hash,
        prevTxIndex: Int, prevTx: Hash,
        txIndex: Int, tx: Hash
    ): StorageAwareTransactionOutputImpl =
        createSA(
            factory.create(
                payout, prevTxBlock,
                prevTxIndex, prevTx,
                txIndex, tx
            )
        )

    override fun create(
        other: TransactionOutput
    ): StorageAwareTransactionOutputImpl =
        createSA(factory.create(other))
}