package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.PollutionAQData
import org.knowledger.ledger.data.PollutionType
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData

class PollutionAQDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<PollutionAQData>(PollutionAQData::class, hashers) {
    override val serializer: KSerializer<PollutionAQData> get() = PollutionAQData.serializer()

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

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as PollutionAQData).let { pollutionData ->
            val parameter = when (pollutionData.parameter) {
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
            session.newInstance(id)
                .setStorageProperty("lastUpdated", pollutionData.lastUpdated)
                .setStorageProperty("unit", pollutionData.unit)
                //Byte encode the enum.
                .setStorageProperty("parameter", parameter)
                .setStorageProperty("value", pollutionData.value)
                .setStorageProperty("sourceName", pollutionData.sourceName)
                .setStorageProperty("city", pollutionData.city)
                .setStorageProperty("citySeqNum", pollutionData.citySeqNum)
        }


    override fun load(element: StorageElement): Outcome<PollutionAQData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("parameter")
            val parameter = when (prop) {
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
            val lastUpdated = getStorageProperty<String>("lastUpdated")
            val unit = getStorageProperty<String>("unit")
            val value = getStorageProperty<Double>("value")
            val sourceName = getStorageProperty<String>("sourceName")
            val city = getStorageProperty<String>("city")
            val citySeqNum = getStorageProperty<Int>("citySeqNum")
            parameter?.let {
                PollutionAQData(lastUpdated, unit, parameter, value, sourceName, city,
                                citySeqNum).ok()
            } ?: DataFailure.UnrecognizedUnit(
                "Parameter is not one of the expected types: $prop"
            ).err()
        }

}