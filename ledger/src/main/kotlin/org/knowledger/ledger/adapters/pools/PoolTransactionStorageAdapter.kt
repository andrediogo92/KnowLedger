package org.knowledger.ledger.adapters.pools

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
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
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class PoolTransactionStorageAdapter : LedgerStorageAdapter<PoolTransaction> {
    override val id: String
        get() = "PoolTransaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "transaction" to StorageType.LINK,
            "inBlock" to StorageType.BOOLEAN
        )

    override fun store(
        element: PoolTransaction, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewLinked("transaction", element.transaction, AdapterIds.Transaction)
                pushNewNative("inBlock", element.inBlock)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<PoolTransaction, LoadFailure> =
        tryOrLoadUnknownFailure {
            val transaction = element.getLinked("transaction")
            val confirmed: Boolean = element.getStorageProperty("inBlock")

            context.transactionStorageAdapter.load(
                ledgerHash, transaction, context
            ).map {
                context.poolTransactionFactory.create(it, confirmed)
            }
        }

}