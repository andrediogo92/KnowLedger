package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.loadTransactionOutputByImpl
import org.knowledger.ledger.storage.transaction.output.store

internal object TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "prevCoinbase" to StorageType.HASH,
            "hash" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "txSet" to StorageType.SET
        )

    override fun store(
        toStore: TransactionOutput,
        session: ManagedSession
    ): StorageElement =
        toStore.store(session)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionOutput, LoadFailure> =
        element.loadTransactionOutputByImpl(ledgerHash)
}