package org.knowledger.ledger.adapters.config

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.results.LoadFailure

internal class CoinbaseParamsStorageAdapter : LedgerStorageAdapter<CoinbaseParams> {
    override val id: String get() = "CoinbaseParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hashSize" to StorageType.INTEGER,
            "timeIncentive" to StorageType.LONG,
            "valueIncentive" to StorageType.LONG,
            "baseIncentive" to StorageType.LONG,
            "dividingThreshold" to StorageType.LONG,
        )

    override fun store(element: CoinbaseParams, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("hashSize", element.hashSize)
            pushNewNative("timeIncentive", element.timeIncentive)
            pushNewNative("valueIncentive", element.valueIncentive)
            pushNewNative("baseIncentive", element.baseIncentive)
            pushNewNative("dividingThreshold", element.dividingThreshold)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<CoinbaseParams, LoadFailure> =
        with(element) {
            val hashSize: Int = getStorageProperty("hashSize")
            val timeIncentive: Long = getStorageProperty("timeIncentive")
            val valueIncentive: Long = getStorageProperty("valueIncentive")
            val baseIncentive: Long = getStorageProperty("baseIncentive")
            val dividingThreshold: Long = getStorageProperty("dividingThreshold")
            context.coinbaseParamsFactory.create(
                hashSize, timeIncentive, valueIncentive, baseIncentive, dividingThreshold,
            ).ok()
        }

}