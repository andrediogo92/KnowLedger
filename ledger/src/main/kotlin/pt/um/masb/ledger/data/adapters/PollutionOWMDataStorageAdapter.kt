package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.PollutionOWMData
import pt.um.masb.ledger.data.PollutionType

class PollutionOWMDataStorageAdapter : AbstractStorageAdapter<PollutionOWMData>(
    PollutionOWMData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "unit" to StorageType.STRING,
            "parameter" to StorageType.INTEGER,
            "value" to StorageType.DOUBLE,
            "data" to StorageType.LISTEMBEDDED,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.STRING
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val pollutionData = toStore as PollutionOWMData
        return session.newInstance(id).apply {
            val parameter = when (pollutionData.parameter) {
                PollutionType.O3 -> PollutionType.O3.ordinal
                PollutionType.UV -> PollutionType.UV.ordinal
                PollutionType.CO -> PollutionType.CO.ordinal
                PollutionType.SO2 -> PollutionType.SO2.ordinal
                PollutionType.NO2 -> PollutionType.NO2.ordinal
                PollutionType.NA -> PollutionType.NA.ordinal
                else -> Int.MAX_VALUE
            }
            setStorageProperty("parameter", parameter)
            setStorageProperty("value", pollutionData.value)
            setStorageProperty("unit", pollutionData.unit)
            setStorageProperty("city", pollutionData.city)
            setStorageProperty("data", emptyList<List<Double>>())
            setStorageProperty("citySeqNum", pollutionData.citySeqNum)
        }
    }

    override fun load(
        element: StorageElement
    ): DataResult<PollutionOWMData> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("parameter")
            val param = when (prop) {
                PollutionType.O3.ordinal -> PollutionType.O3
                PollutionType.UV.ordinal -> PollutionType.UV
                PollutionType.CO.ordinal -> PollutionType.CO
                PollutionType.SO2.ordinal -> PollutionType.SO2
                PollutionType.NO2.ordinal -> PollutionType.NO2
                PollutionType.NA.ordinal -> PollutionType.NA
                else -> null
            }
            if (param == null) {
                DataResult.UnrecognizedUnit<PollutionOWMData>(
                    "Parameter is not one of the expected types: $prop"
                )
            } else {
                DataResult.Success(
                    PollutionOWMData(
                        getStorageProperty("unit"),
                        param,
                        getStorageProperty("value"),
                        getStorageProperty("data"),
                        getStorageProperty("city"),
                        getStorageProperty("citySeqNum")
                    )
                )
            }
        }
}