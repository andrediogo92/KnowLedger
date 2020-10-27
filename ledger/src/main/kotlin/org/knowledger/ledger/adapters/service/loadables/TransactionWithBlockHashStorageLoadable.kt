package org.knowledger.ledger.adapters.service.loadables

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.service.ServiceLoadable
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.data.TransactionWithBlockHash
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.results.LoadFailure

internal class TransactionWithBlockHashStorageLoadable : ServiceLoadable<TransactionWithBlockHash> {
    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<TransactionWithBlockHash, LoadFailure> =
        with(element) {
            val txDataElement = getLinked("txData")
            context.physicalDataStorageAdapter
                .load(ledgerHash, txDataElement, context)
                .map { txData ->
                    val txBlockHash = getHashProperty("txBlockHash")
                    val txHash = getHashProperty("txHash")
                    val txIndex = getStorageProperty<Int>("txIndex")
                    val txMillis = getStorageProperty<Long>("txMillis")
                    val txMin = getStorageProperty<Long>("txMin")
                    TransactionWithBlockHash(txBlockHash, txHash, txIndex, txMillis, txMin, txData)
                }

        }
}