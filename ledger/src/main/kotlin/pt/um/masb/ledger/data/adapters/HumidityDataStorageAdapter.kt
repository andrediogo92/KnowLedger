package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataResult
import pt.um.masb.ledger.data.HUnit
import pt.um.masb.ledger.data.HumidityData

class HumidityDataStorageAdapter : AbstractStorageAdapter<HumidityData>(
    HumidityData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "hum" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: BlockChainData, session: NewInstanceSession
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
    ): DataResult<HumidityData> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                HUnit.G_BY_KG.ordinal -> HUnit.G_BY_KG
                HUnit.KG_BY_KG.ordinal -> HUnit.KG_BY_KG
                HUnit.RELATIVE.ordinal -> HUnit.RELATIVE
                else -> null
            }
            if (unit == null) {
                DataResult.UnrecognizedUnit<HumidityData>(
                    "HUnit is not one of the expected: $prop"
                )
            } else {
                DataResult.Success(
                    HumidityData(
                        getStorageProperty("hum"),
                        unit
                    )
                )
            }
        }

}