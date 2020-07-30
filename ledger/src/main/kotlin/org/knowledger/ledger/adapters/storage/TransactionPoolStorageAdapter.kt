package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewLinkedList
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransactionPool
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class TransactionPoolStorageAdapter : LedgerStorageAdapter<MutableTransactionPool> {
    override val id: String
        get() = "TransactionPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "transactions" to StorageType.LIST
        )

    override fun store(
        element: MutableTransactionPool, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
                pushNewLinkedList(
                    "transactions", element.mutableTransactions, AdapterIds.Transaction
                )
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<MutableTransactionPool, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val chainIdElem = element.getLinked("chainId")
                val transactionsElem = element.getElementList("transactions")

                binding<MutableTransactionPool, LoadFailure> {
                    val chainId = context.chainIdStorageAdapter.load(
                        ledgerHash, chainIdElem, context
                    ).bind()
                    val transactions = transactionsElem.map {
                        context.poolTransactionStorageAdapter.load(
                            ledgerHash, it, context
                        )
                    }.combine().bind()
                    context.transactionPoolFactory.create(
                        chainId, transactions.toMutableSortedListFromPreSorted()
                    )
                }
            }
        }
}