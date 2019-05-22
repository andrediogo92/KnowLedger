package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.TUnit
import pt.um.masb.ledger.data.TemperatureData
import pt.um.masb.ledger.results.tryOrDataQueryFailure

class TemperatureDataStorageAdapter : AbstractStorageAdapter<TemperatureData>(
    TemperatureData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "temperature" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val temperatureData = toStore as TemperatureData
        return session.newInstance(id).apply {
            setStorageProperty(
                "temperature",
                temperatureData.temperature
            )
            setStorageProperty(
                "unit",
                when (temperatureData.unit) {
                    TUnit.CELSIUS -> TUnit.CELSIUS.ordinal
                    TUnit.FAHRENHEIT -> TUnit.FAHRENHEIT.ordinal
                    TUnit.KELVIN -> TUnit.KELVIN.ordinal
                    TUnit.RANKINE -> TUnit.RANKINE.ordinal
                }
            )
        }
    }

    override fun load(
        element: StorageElement
    ): DataResult<TemperatureData> =
        tryOrDataQueryFailure {
            val prop = element.getStorageProperty<Int>("unit")
            val unit = when (prop) {
                TUnit.CELSIUS.ordinal -> TUnit.CELSIUS
                TUnit.FAHRENHEIT.ordinal -> TUnit.FAHRENHEIT
                TUnit.KELVIN.ordinal -> TUnit.KELVIN
                TUnit.RANKINE.ordinal -> TUnit.RANKINE
                else -> null
            }
            if (unit == null) {
                DataResult.UnrecognizedUnit<TemperatureData>(
                    "Unit is not one of the expected: $prop"
                )
            } else {
                DataResult.Success(
                    TemperatureData(
                        element.getStorageProperty("temperature"),
                        unit
                    )
                )
            }
        }

}