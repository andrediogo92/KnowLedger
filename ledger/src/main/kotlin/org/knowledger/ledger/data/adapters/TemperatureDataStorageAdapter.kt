package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.data.TUnit
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.results.tryOrDataUnknownFailure

object TemperatureDataStorageAdapter : AbstractStorageAdapter<TemperatureData>(
    TemperatureData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "temperature" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as TemperatureData).let {
            session
                .newInstance(id)
                .setStorageProperty(
                    "temperature",
                    it.temperature
                ).setStorageProperty(
                    "unit",
                    when (it.unit) {
                        TUnit.CELSIUS -> TUnit.CELSIUS.ordinal
                        TUnit.FAHRENHEIT -> TUnit.FAHRENHEIT.ordinal
                        TUnit.KELVIN -> TUnit.KELVIN.ordinal
                        TUnit.RANKINE -> TUnit.RANKINE.ordinal
                    }
                )
        }


    override fun load(
        element: StorageElement
    ): Outcome<TemperatureData, DataFailure> =
        tryOrDataUnknownFailure {
            val prop = element.getStorageProperty<Int>("unit")
            val unit = when (prop) {
                TUnit.CELSIUS.ordinal -> TUnit.CELSIUS
                TUnit.FAHRENHEIT.ordinal -> TUnit.FAHRENHEIT
                TUnit.KELVIN.ordinal -> TUnit.KELVIN
                TUnit.RANKINE.ordinal -> TUnit.RANKINE
                else -> null
            }
            if (unit == null) {
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "Unit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    TemperatureData(
                        element.getStorageProperty("temperature"),
                        unit
                    )
                )
            }
        }

}