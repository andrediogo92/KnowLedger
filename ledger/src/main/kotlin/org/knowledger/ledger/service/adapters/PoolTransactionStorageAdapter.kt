package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapFailure
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.intoLedger
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.pools.transaction.PoolTransaction
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.storage.adapters.loadTransaction
import org.knowledger.ledger.storage.adapters.persist

internal object PoolTransactionStorageAdapter : ServiceStorageAdapter<PoolTransaction> {
    override val id: String = "PoolTransaction"
    override val properties: Map<String, StorageType> =
        mapOf(
            "transaction" to StorageType.LINK,
            "confirmed" to StorageType.BOOLEAN
        )

    override fun store(
        toStore: PoolTransaction,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked("transaction", toStore.transaction.persist(session))
            .setStorageProperty("confirmed", toStore.confirmed)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<PoolTransaction, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val transaction = element.getLinked("transaction")
            val confirmed: Boolean = element.getStorageProperty("confirmed")

            transaction.loadTransaction(ledgerHash).mapSuccess {
                PoolTransaction(
                    it,
                    confirmed
                )
            }.mapFailure {
                it.intoLedger()
            }
        }

}