package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.config.adapters.loadChainId
import org.knowledger.ledger.config.adapters.persist
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.adapters.TransactionPoolStorageAdapter
import org.knowledger.ledger.service.adapters.loadPoolTransaction
import org.knowledger.ledger.service.adapters.persist
import org.knowledger.ledger.service.results.LedgerFailure

internal object SUTransactionPoolStorageAdapter : ServiceStorageAdapter<TransactionPoolImpl> {
    override val id: String
        get() = TransactionPoolStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionPoolStorageAdapter.properties

    override fun store(
        toStore: TransactionPoolImpl,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "chainId",
                toStore.chainId.persist(session)
            ).setElementSet(
                "transactions",
                toStore.transactions.map {
                    it.persist(session)
                }.toSet()
            )

    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionPoolImpl, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val chainId = element.getLinked("chainId")
            val transactions = element.getElementSet("transactions")

            zip(
                chainId.loadChainId(ledgerHash),
                transactions.map {
                    it.loadPoolTransaction(ledgerHash)
                }.allValues()
            ) { chainId, transactions ->
                TransactionPoolImpl(
                    chainId,
                    transactions.toMutableSet()
                )
            }
        }
}