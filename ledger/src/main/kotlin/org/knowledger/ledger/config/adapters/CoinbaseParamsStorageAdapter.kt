package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object CoinbaseParamsStorageAdapter : ServiceStorageAdapter<CoinbaseParams> {
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
        toStore: CoinbaseParams, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "hashSize", toStore.hashSize
            ).setStorageProperty(
                "timeIncentive", toStore.timeIncentive
            ).setStorageProperty(
                "valueIncentive", toStore.valueIncentive
            ).setStorageProperty(
                "baseIncentive", toStore.baseIncentive
            ).setStorageProperty(
                "dividingThreshold", toStore.dividingThreshold
            )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<CoinbaseParams, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            Outcome.Ok(
                CoinbaseParams(
                    hashSize = element.getStorageProperty("hashSize"),
                    timeIncentive = element.getStorageProperty("timeIncentive"),
                    valueIncentive = element.getStorageProperty("valueIncentive"),
                    baseIncentive = element.getStorageProperty("baseIncentive"),
                    dividingThreshold = element.getStorageProperty("dividingThreshold")
                )
            )
        }

}