package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.results.zip
import org.knowledger.ledger.service.adapters.PoolTransactionStorageAdapter
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal class SUTransactionPoolStorageAdapter(
    private val adapterManager: AdapterManager,
    private val poolTransactionStorageAdapter: PoolTransactionStorageAdapter
) : ServiceStorageAdapter<TransactionPoolImpl> {
    override val id: String
        get() = "TransactionPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "transactions" to StorageType.SET
        )

    override fun store(
        toStore: TransactionPoolImpl,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "chainId",
                ChainIdStorageAdapter.persist(
                    toStore.chainId, session
                )
            ).setElementSet(
                "transactions",
                toStore.transactions.map {
                    poolTransactionStorageAdapter.persist(
                        it, session
                    )
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
                ChainIdStorageAdapter.load(
                    ledgerHash, chainId
                ),
                transactions.map {
                    poolTransactionStorageAdapter.load(
                        ledgerHash,
                        it
                    )
                }.allValues()
            ) { chainId, transactions ->
                TransactionPoolImpl(
                    adapterManager,
                    chainId,
                    transactions.toMutableSet()
                )
            }
        }
}