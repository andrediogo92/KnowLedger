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
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class CoinbaseParamsStorageAdapter : LedgerStorageAdapter<CoinbaseParams> {
    override val id: String
        get() = "CoinbaseParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hashSize" to StorageType.INTEGER,
            "timeIncentive" to StorageType.LONG,
            "valueIncentive" to StorageType.LONG,
            "baseIncentive" to StorageType.LONG,
            "dividingThreshold" to StorageType.LONG
        )

    override fun store(
        element: CoinbaseParams, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("hashSize", element.hashSize)
                pushNewNative("timeIncentive", element.timeIncentive)
                pushNewNative("valueIncentive", element.valueIncentive)
                pushNewNative("baseIncentive", element.baseIncentive)
                pushNewNative("dividingThreshold", element.dividingThreshold)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<CoinbaseParams, LoadFailure> =
        tryOrLoadUnknownFailure {
            val hashSize: Int = element.getStorageProperty("hashSize")
            val timeIncentive: Long = element.getStorageProperty("timeIncentive")
            val valueIncentive: Long = element.getStorageProperty("valueIncentive")
            val baseIncentive: Long = element.getStorageProperty("baseIncentive")
            val dividingThreshold: Long = element.getStorageProperty("dividingThreshold")
            context.coinbaseParamsFactory.create(
                hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold
            ).ok()
        }

}