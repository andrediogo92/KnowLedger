package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.PollutionOWMData
import org.knowledger.ledger.data.PollutionType

object PollutionOWMDataStorageAdapter : AbstractStorageAdapter<PollutionOWMData>(
    PollutionOWMData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "unit" to StorageType.STRING,
            "parameter" to StorageType.INTEGER,
            "value" to StorageType.DOUBLE,
            "value" to StorageType.LISTEMBEDDED,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.STRING
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
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
            setStorageProperty("value", emptyList<List<Double>>())
            setStorageProperty("citySeqNum", pollutionData.citySeqNum)
        }
    }

    override fun load(
        element: StorageElement
    ): Outcome<PollutionOWMData, DataFailure> =
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
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "Parameter is not one of the expected types: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    PollutionOWMData(
                        getStorageProperty("unit"),
                        param,
                        getStorageProperty("value"),
                        getStorageProperty("value"),
                        getStorageProperty("city"),
                        getStorageProperty("citySeqNum")
                    )
                )
            }
        }
}