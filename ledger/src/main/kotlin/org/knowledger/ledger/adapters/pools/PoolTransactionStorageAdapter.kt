package org.knowledger.ledger.adapters.pools

import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.PoolTransaction
import org.knowledger.ledger.storage.results.LoadFailure

internal class PoolTransactionStorageAdapter : LedgerStorageAdapter<PoolTransaction> {
    override val id: String get() = "PoolTransaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "transaction" to StorageType.LINK,
            "inBlock" to StorageType.BOOLEAN,
        )

    override fun store(
        element: PoolTransaction, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("transaction", element.transaction, AdapterIds.Transaction)
            pushNewNative("inBlock", element.inBlock)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<PoolTransaction, LoadFailure> =
        with(element) {
            val transaction = getLinked("transaction")
            val confirmed: Boolean = getStorageProperty("inBlock")

            context.transactionStorageAdapter
                .load(ledgerHash, transaction, context)
                .map { context.poolTransactionFactory.create(it, confirmed) }
        }

}