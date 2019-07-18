package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
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
    ): StorageElement =
        (toStore as PollutionAQData).let {
            session
                .newInstance(id)
                .setStorageProperty("lastUpdated", it.lastUpdated)
                .setStorageProperty("unit", it.unit)
                //Byte encode the enum.
                .setStorageProperty(
                    "parameter", when (it.parameter) {
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
                ).setStorageProperty("value", it.value)
                .setStorageProperty("sourceName", it.sourceName)
                .setStorageProperty("city", it.city)
                .setStorageProperty("citySeqNum", it.citySeqNum)
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