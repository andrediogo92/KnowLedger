package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.CoinbaseParams
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

    override fun store(toStore: CoinbaseParams, session: NewInstanceSession): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "timeIncentive", toStore.timeIncentive
            ).setStorageProperty(
                "valueIncentive", toStore.valueIncentive
            ).setStorageProperty(
                "baseIncentive", toStore.baseIncentive
            ).setStorageProperty(
                "dividingThreshold", toStore.dividingThreshold
            )
        }

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