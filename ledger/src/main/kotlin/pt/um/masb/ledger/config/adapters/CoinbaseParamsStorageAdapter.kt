package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.service.adapters.ServiceStorageAdapter
import pt.um.masb.ledger.service.results.LedgerFailure

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