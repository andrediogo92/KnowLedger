package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object CoinbaseParamsStorageAdapter : ServiceStorageAdapter<CoinbaseParams> {
    override val id: String
        get() = "CoinbaseParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
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
                    element.getStorageProperty("timeIncentive"),
                    element.getStorageProperty("valueIncentive"),
                    element.getStorageProperty("baseIncentive"),
                    element.getStorageProperty("dividingThreshold")
                )
            )
        }

}