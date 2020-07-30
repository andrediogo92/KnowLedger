package org.knowledger.ledger.adapters.storage


import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.service.solver.pushNewPayout
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payout" to StorageType.PAYOUT,
            "prevTxBlock" to StorageType.HASH,
            "prevTxIndex" to StorageType.INTEGER,
            "prevTx" to StorageType.HASH,
            "txIndex" to StorageType.INTEGER,
            "tx" to StorageType.HASH
        )

    override fun store(
        element: TransactionOutput, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewPayout("payout", element.payout)
                pushNewHash("prevTxBlock", element.prevTxBlock)
                pushNewNative("prevTxIndex", element.prevTxIndex)
                pushNewHash("prevTx", element.prevTx)
                pushNewNative("txIndex", element.txIndex)
                pushNewHash("tx", element.tx)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<TransactionOutput, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val payout: Payout = element.getPayoutProperty("payout")
                val prevTxBlock: Hash = element.getHashProperty("prevTxBlock")
                val prevTxIndex: Int = element.getStorageProperty("prevTxIndex")
                val prevTx: Hash = element.getHashProperty("prevTx")
                val txIndex: Int = element.getStorageProperty("txIndex")
                val tx: Hash = element.getHashProperty("tx")
                context.transactionOutputFactory.create(
                    payout = payout, prevTxBlock = prevTxBlock,
                    prevTxIndex = prevTxIndex, prevTx = prevTx,
                    txIndex = txIndex, tx = tx
                ).ok()
            }
        }
}