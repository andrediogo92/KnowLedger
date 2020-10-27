package org.knowledger.ledger.adapters.storage


import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.results.LoadFailure

internal class TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payout" to StorageType.PAYOUT,
            "prevTxBlock" to StorageType.HASH,
            "prevTxIndex" to StorageType.INTEGER,
            "prevTx" to StorageType.HASH,
            "txIndex" to StorageType.INTEGER,
            "tx" to StorageType.HASH,
        )

    override fun store(
        element: TransactionOutput, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewPayout("payout", element.payout)
            pushNewHash("prevTxBlock", element.prevTxBlock)
            pushNewNative("prevTxIndex", element.prevTxIndex)
            pushNewHash("prevTx", element.prevTx)
            pushNewNative("txIndex", element.txIndex)
            pushNewHash("tx", element.tx)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<TransactionOutput, LoadFailure> =
        element.cachedLoad {
            val payout: Payout = getPayoutProperty("payout")
            val prevTxBlock: Hash = getHashProperty("prevTxBlock")
            val prevTxIndex: Int = getStorageProperty("prevTxIndex")
            val prevTx: Hash = getHashProperty("prevTx")
            val txIndex: Int = getStorageProperty("txIndex")
            val tx: Hash = getHashProperty("tx")
            context.transactionOutputFactory.create(
                payout, prevTxBlock, prevTxIndex, prevTx, txIndex, tx,
            ).ok()
        }
}