package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.HUnit
import org.knowledger.ledger.data.HumidityData

object HumidityDataStorageAdapter : AbstractStorageAdapter<HumidityData>(
    HumidityData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hum" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement {
        val humidityData = toStore as HumidityData
        return session.newInstance(id).apply {
            setStorageProperty("hum", humidityData.hum)
            val hUnit = when (humidityData.unit) {
                HUnit.G_BY_KG -> HUnit.G_BY_KG.ordinal
                HUnit.KG_BY_KG -> HUnit.KG_BY_KG.ordinal
                HUnit.RELATIVE -> HUnit.RELATIVE.ordinal
            }
            setStorageProperty("unit", hUnit)
        }
    }

    override fun load(
        element: StorageElement
    ): Outcome<HumidityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                HUnit.G_BY_KG.ordinal -> HUnit.G_BY_KG
                HUnit.KG_BY_KG.ordinal -> HUnit.KG_BY_KG
                HUnit.RELATIVE.ordinal -> HUnit.RELATIVE
                else -> null
            }
            if (unit == null) {
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "HUnit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    HumidityData(
                        getStorageProperty("hum"),
                        unit
                    )
                )
            }
        }

}