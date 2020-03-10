package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.service.transactions.TransactionWithBlockHash
import org.knowledger.ledger.storage.adapters.StorageLoadable

internal class TransactionWithBlockHashStorageLoadable(
    val physicalDataStorageAdapter: PhysicalDataStorageAdapter
) : StorageLoadable<TransactionWithBlockHash> {
    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<TransactionWithBlockHash, LoadFailure> =
        tryOrLoadUnknownFailure {
            val txDataElement = element.getLinked("txData")
            physicalDataStorageAdapter
                .load(ledgerHash, txDataElement)
                .mapSuccess { txData ->
                    val txBlockHash = element.getHashProperty("txBlockHash")
                    val txHash = element.getHashProperty("txHash")
                    val txIndex = element.getStorageProperty<Int>("txIndex")
                    val txMillis = element.getStorageProperty<Long>("txMillis")
                    val txMin = element.getStorageProperty<Long>("txMin")
                    TransactionWithBlockHash(
                        txBlockHash = txBlockHash, txHash = txHash,
                        txIndex = txIndex, txMillis = txMillis,
                        txMin = txMin, txData = txData
                    )
                }

        }

}