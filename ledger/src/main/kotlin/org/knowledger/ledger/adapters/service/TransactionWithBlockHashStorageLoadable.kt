package org.knowledger.ledger.adapters.service

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.StorageLoadable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.transactions.TransactionWithBlockHash
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class TransactionWithBlockHashStorageLoadable :
    StorageLoadable<TransactionWithBlockHash> {
    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<TransactionWithBlockHash, LoadFailure> =
        tryOrLoadUnknownFailure {
            val txDataElement = element.getLinked("txData")
            context.physicalDataStorageAdapter
                .load(ledgerHash, txDataElement, context)
                .map { txData ->
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