package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.crypto.hash.Hashers.SHA3512Hasher
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.PollutionOWMData
import org.knowledger.ledger.data.PollutionType
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.adapters.AbstractStorageAdapter
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome

object PollutionOWMDataStorageAdapter : AbstractStorageAdapter<PollutionOWMData>(
    PollutionOWMData::class.java,
    SHA3512Hasher
) {
    override val serializer: KSerializer<PollutionOWMData>
        get() = PollutionOWMData.serializer()

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
    ): StorageElement =
        (toStore as PollutionOWMData).let {
            session
                .newInstance(id)
                .setStorageProperty(
                    "parameter", when (it.parameter) {
                        PollutionType.O3 -> PollutionType.O3.ordinal
                        PollutionType.UV -> PollutionType.UV.ordinal
                        PollutionType.CO -> PollutionType.CO.ordinal
                        PollutionType.SO2 -> PollutionType.SO2.ordinal
                        PollutionType.NO2 -> PollutionType.NO2.ordinal
                        PollutionType.NA -> PollutionType.NA.ordinal
                        else -> Int.MAX_VALUE
                    }
                )
                .setStorageProperty("value", it.value)
                .setStorageProperty("unit", it.unit)
                .setStorageProperty("city", it.city)
                .setStorageProperty("value", emptyList<List<Double>>())
                .setStorageProperty("citySeqNum", it.citySeqNum)
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