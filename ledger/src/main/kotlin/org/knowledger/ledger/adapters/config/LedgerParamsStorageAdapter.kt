package org.knowledger.ledger.adapters.config

import org.knowledger.ledger.adapters.service.HandleStorageAdapter
import org.knowledger.ledger.adapters.service.LedgerMagicPair
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.results.LoadFailure

internal class LedgerParamsStorageAdapter : HandleStorageAdapter<LedgerParams> {
    override val id: String get() = "LedgerParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hashers" to StorageType.HASH,
            "recalculationTime" to StorageType.LONG,
            "recalculationTrigger" to StorageType.INTEGER,
        )

    override fun store(element: LedgerParams, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewHash("hashers", element.hashers)
            pushNewNative("recalculationTime", element.recalculationTime)
            pushNewNative("recalculationTrigger", element.recalculationTrigger)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: LedgerMagicPair,
    ): Outcome<LedgerParams, LoadFailure> =
        with(element) {
            val hashers: Hash = getHashProperty("hashers")
            val recalculationTime: Long = getStorageProperty("recalculationTime")
            val recalculationTrigger: Int = getStorageProperty("recalculationTrigger")
            context.factory.create(hashers, recalculationTime, recalculationTrigger).ok()
        }
}