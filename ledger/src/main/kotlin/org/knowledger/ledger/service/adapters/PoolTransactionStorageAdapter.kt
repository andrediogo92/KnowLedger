package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.intoLedger
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.pools.transaction.PoolTransaction
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter

internal class PoolTransactionStorageAdapter(
    private val adapterManager: AdapterManager,
    private val transactionStorageAdapter: TransactionStorageAdapter
) : ServiceStorageAdapter<PoolTransaction> {
    override val id: String
        get() = "PoolTransaction"
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "transaction" to StorageType.LINK,
            "confirmed" to StorageType.BOOLEAN
        )

    override fun store(
        toStore: PoolTransaction,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "transaction",
                transactionStorageAdapter.persist(
                    toStore.transaction, session
                )
            ).setStorageProperty("confirmed", toStore.confirmed)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<PoolTransaction, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val transaction = element.getLinked("transaction")
            val confirmed: Boolean = element.getStorageProperty("confirmed")

            transactionStorageAdapter.load(
                ledgerHash, transaction
            ).mapSuccess {
                PoolTransaction(
                    adapterManager,
                    it,
                    confirmed
                )
            }.mapFailure {
                it.intoLedger()
            }
        }

}