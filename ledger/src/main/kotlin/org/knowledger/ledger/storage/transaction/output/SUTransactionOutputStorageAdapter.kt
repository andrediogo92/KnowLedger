package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SUTransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutputImpl> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payout" to StorageType.DECIMAL,
            "prevTxBlock" to StorageType.HASH,
            "prevTxIndex" to StorageType.INTEGER,
            "prevTx" to StorageType.HASH,
            "txIndex" to StorageType.INTEGER,
            "tx" to StorageType.HASH
        )

    override fun store(
        toStore: TransactionOutputImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "payout", toStore.payout
            ).setHashProperty(
                "prevTxBlock", toStore.prevTxBlock
            ).setStorageProperty(
                "prevTxIndex", toStore.prevTxIndex
            ).setHashProperty(
                "prevTx", toStore.prevTx
            ).setStorageProperty(
                "txIndex", toStore.txIndex
            )
            .setHashProperty("tx", toStore.tx)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<TransactionOutputImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val payout: Payout = Payout(
                element.getStorageProperty("payout")
            )
            val prevTxBlock: Hash =
                element.getHashProperty("prevTxBlock")
            val prevTxIndex: Int =
                element.getStorageProperty("prevTxIndex")
            val prevTx: Hash =
                element.getHashProperty("prevTx")
            val txIndex: Int =
                element.getStorageProperty("txIndex")
            val tx: Hash =
                element.getHashProperty("tx")
            Outcome.Ok(
                TransactionOutputImpl(
                    payout = payout, prevTxBlock = prevTxBlock,
                    prevTxIndex = prevTxIndex, prevTx = prevTx,
                    txIndex = txIndex, tx = tx
                )
            )
        }

}