package org.knowledger.ledger.adapters.config

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
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class LedgerParamsStorageAdapter : LedgerStorageAdapter<LedgerParams> {
    override val id: String
        get() = "LedgerParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hashers" to StorageType.HASH,
            "recalculationTime" to StorageType.LONG,
            "recalculationTrigger" to StorageType.INTEGER
        )

    override fun store(
        element: LedgerParams, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewHash("hashers", element.hashers)
                pushNewNative("recalculationTime", element.recalculationTime)
                pushNewNative("recalculationTrigger", element.recalculationTrigger)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<LedgerParams, LoadFailure> =
        tryOrLoadUnknownFailure {
            val hashers: Hash = element.getHashProperty("hashers")
            val recalculationTime: Long = element.getStorageProperty("recalculationTime")
            val recalculationTrigger: Int = element.getStorageProperty("recalculationTrigger")
            context.ledgerParamsFactory.create(
                hashers, recalculationTime, recalculationTrigger
            ).ok()
        }
}