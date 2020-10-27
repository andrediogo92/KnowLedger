package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.PollutionOWMData
import org.knowledger.ledger.data.PollutionType
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData

class PollutionOWMDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<PollutionOWMData>(PollutionOWMData::class, hashers) {
    override val serializer: KSerializer<PollutionOWMData> get() = PollutionOWMData.serializer()

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "unit" to StorageType.STRING,
            "parameter" to StorageType.INTEGER,
            "value" to StorageType.DOUBLE,
            "data" to StorageType.LISTEMBEDDED,
            "city" to StorageType.STRING,
            "citySeqNum" to StorageType.STRING
        )

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as PollutionOWMData).let { pollutionOWMData ->
            val parameter = when (pollutionOWMData.parameter) {
                PollutionType.O3 -> PollutionType.O3.ordinal
                PollutionType.UV -> PollutionType.UV.ordinal
                PollutionType.CO -> PollutionType.CO.ordinal
                PollutionType.SO2 -> PollutionType.SO2.ordinal
                PollutionType.NO2 -> PollutionType.NO2.ordinal
                PollutionType.NA -> PollutionType.NA.ordinal
                else -> Int.MAX_VALUE
            }
            session.newInstance(id)
                .setStorageProperty("parameter", parameter)
                .setStorageProperty("value", pollutionOWMData.value)
                .setStorageProperty("unit", pollutionOWMData.unit)
                .setStorageProperty("city", pollutionOWMData.city)
                .setStorageProperty("data", emptyList<List<Double>>())
                .setStorageProperty("citySeqNum", pollutionOWMData.citySeqNum)
        }


    override fun load(element: StorageElement): Outcome<PollutionOWMData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("parameter")
            val parameter = when (prop) {
                PollutionType.O3.ordinal -> PollutionType.O3
                PollutionType.UV.ordinal -> PollutionType.UV
                PollutionType.CO.ordinal -> PollutionType.CO
                PollutionType.SO2.ordinal -> PollutionType.SO2
                PollutionType.NO2.ordinal -> PollutionType.NO2
                PollutionType.NA.ordinal -> PollutionType.NA
                else -> null
            }
            val unit = getStorageProperty<String>("unit")
            val value = getStorageProperty<Double>("value")
            val data = getStorageProperty<List<List<Double>>>("value")
            val city = getStorageProperty<String>("city")
            val citySeqNum = getStorageProperty<Int>("citySeqNum")
            parameter?.let {
                PollutionOWMData(unit, parameter, value, data, city, citySeqNum).ok()
            } ?: DataFailure.UnrecognizedUnit(
                "Parameter is not one of the expected types: $prop"
            ).err()
        }
}