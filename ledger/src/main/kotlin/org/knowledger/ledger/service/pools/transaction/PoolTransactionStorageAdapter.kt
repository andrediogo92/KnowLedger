package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object PoolTransactionStorageAdapter : ServiceStorageAdapter<PoolTransaction> {
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
            .setLinkedID("transaction", toStore.id)
            .setStorageProperty("confirmed", toStore.confirmed)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<PoolTransaction, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val transaction = element.getLinkedID("transaction")
            val confirmed: Boolean = element.getStorageProperty("confirmed")

            Outcome.Ok(
                PoolTransaction(
                    transaction,
                    confirmed
                )
            )
        }

}