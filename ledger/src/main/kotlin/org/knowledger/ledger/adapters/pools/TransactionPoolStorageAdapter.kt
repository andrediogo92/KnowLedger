package org.knowledger.ledger.adapters.pools

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.results.LoadFailure

internal class TransactionPoolStorageAdapter : LedgerStorageAdapter<MutableTransactionPool> {
    override val id: String get() = "TransactionPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "transactions" to StorageType.LIST,
        )

    override fun store(
        element: MutableTransactionPool, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
            pushNewLinkedList("transactions", element.mutableTransactions, AdapterIds.Transaction)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableTransactionPool, LoadFailure> =
        element.cachedLoad {
            val chainIdElem = getLinked("chainId")
            val transactionsElem = getElementList("transactions")

            binding {
                val chainId = context.chainIdStorageAdapter.load(
                    ledgerHash, chainIdElem, context,
                ).bind()
                val transactions = transactionsElem.map {
                    context.poolTransactionStorageAdapter.load(ledgerHash, it, context)
                }.combine().bind()
                context.transactionPoolFactory.create(
                    chainId, transactions.toMutableSortedListFromPreSorted(),
                )
            }
        }
}