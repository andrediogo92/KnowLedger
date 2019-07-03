package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.PollutionAQData
import org.knowledger.ledger.data.PollutionType

object PollutionAQDataStorageAdapter : AbstractStorageAdapter<PollutionAQData>(
    PollutionAQData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "lastUpdated" to StorageType.STRING,
            "unit" to StorageType.STRING,
            "parameter" to StorageType.INTEGER,
            "value" to StorageType.DOUBLE,
            "sourceName" to StorageType.STRING,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement {
        val pollutionData = toStore as PollutionAQData
        return session.newInstance(id).apply {
            setStorageProperty("lastUpdated", pollutionData.lastUpdated)
            setStorageProperty("unit", pollutionData.unit)
            //Byte encode the enum.
            val byte =
                when (pollutionData.parameter) {
                    PollutionType.PM25 -> PollutionType.PM25.ordinal
                    PollutionType.PM10 -> PollutionType.PM10.ordinal
                    PollutionType.SO2 -> PollutionType.SO2.ordinal
                    PollutionType.NO2 -> PollutionType.NO2.ordinal
                    PollutionType.O3 -> PollutionType.O3.ordinal
                    PollutionType.CO -> PollutionType.CO.ordinal
                    PollutionType.BC -> PollutionType.BC.ordinal
                    PollutionType.NA -> PollutionType.NA.ordinal
                    else -> Int.MAX_VALUE
                }
            setStorageProperty("parameter", byte)
            setStorageProperty("value", pollutionData.value)
            setStorageProperty("sourceName", pollutionData.sourceName)
            setStorageProperty("city", pollutionData.city)
            setStorageProperty("citySeqNum", pollutionData.citySeqNum)
        }
    }

    override fun load(
        element: StorageElement
    ): Outcome<PollutionAQData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("parameter")
            val param = when (prop) {
                PollutionType.PM25.ordinal -> PollutionType.PM25
                PollutionType.PM10.ordinal -> PollutionType.PM10
                PollutionType.SO2.ordinal -> PollutionType.SO2
                PollutionType.NO2.ordinal -> PollutionType.NO2
                PollutionType.O3.ordinal -> PollutionType.O3
                PollutionType.CO.ordinal -> PollutionType.CO
                PollutionType.BC.ordinal -> PollutionType.BC
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
                    PollutionAQData(
                        getStorageProperty("lastUpdated"),
                        getStorageProperty("unit"),
                        param,
                        getStorageProperty("value"),
                        getStorageProperty("sourceName"),
                        getStorageProperty("city"),
                        getStorageProperty("citySeqNum")
                    )
                )
            }
        }

}