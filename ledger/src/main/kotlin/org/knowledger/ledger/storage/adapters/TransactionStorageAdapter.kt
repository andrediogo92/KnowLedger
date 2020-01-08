package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction.loadTransactionByImpl
import org.knowledger.ledger.storage.transaction.store

internal object TransactionStorageAdapter : LedgerStorageAdapter<Transaction> {
    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "value" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hash" to StorageType.HASH
        )

    override fun store(
        toStore: Transaction, session: ManagedSession
    ): StorageElement =
        toStore.store(session)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Transaction, LoadFailure> =
        element.loadTransactionByImpl(ledgerHash)
}