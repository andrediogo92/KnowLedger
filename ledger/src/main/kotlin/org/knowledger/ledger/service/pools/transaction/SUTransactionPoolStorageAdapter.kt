package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SUTransactionPoolStorageAdapter :
    ServiceStorageAdapter<TransactionPoolImpl> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: TransactionPoolImpl,
        session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashList("transactions", toStore.transactions)
            .setStorageProperty("confirmations", toStore.confirmations)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionPoolImpl, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            ChainIdStorageAdapter.load(
                ledgerHash, element.getLinked("chainId")
            ).mapSuccess {
                TransactionPoolImpl(
                    it,
                    element.getMutableHashList("transactions"),
                    element.getStorageProperty("confirmations")
                )
            }
        }
}