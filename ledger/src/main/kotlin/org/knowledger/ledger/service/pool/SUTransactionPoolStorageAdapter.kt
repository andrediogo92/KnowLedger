package org.knowledger.ledger.service.pool

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SUTransactionPoolStorageAdapter :
    ServiceStorageAdapter<StorageUnawareTransactionPool> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareTransactionPool,
        session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashList("transactions", toStore.transactions)
            .setStorageProperty("confirmations", toStore.confirmations)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageUnawareTransactionPool, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            ChainIdStorageAdapter.load(
                ledgerHash, element.getLinked("chainId")
            ).mapSuccess {
                StorageUnawareTransactionPool(
                    it,
                    element.getMutableHashList("transactions"),
                    element.getStorageProperty("confirmations")
                )
            }
        }
}