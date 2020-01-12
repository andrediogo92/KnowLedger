package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.PollutionAQData
import org.knowledger.ledger.data.PollutionType
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome

class PollutionAQDataStorageAdapter(hasher: Hashers) : AbstractStorageAdapter<PollutionAQData>(
    PollutionAQData::class.java,
    hasher
) {
    override val serializer: KSerializer<PollutionAQData>
        get() = PollutionAQData.serializer()

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