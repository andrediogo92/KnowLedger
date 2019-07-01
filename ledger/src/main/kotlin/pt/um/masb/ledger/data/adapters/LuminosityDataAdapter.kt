package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.ledger.data.LUnit
import pt.um.masb.ledger.data.LuminosityData

object LuminosityDataAdapter : AbstractStorageAdapter<LuminosityData>(
    LuminosityData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "lum" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement {
        val luminosityData = toStore as LuminosityData
        return session.newInstance(id).apply {
            setStorageProperty("lum", luminosityData.lum)
            setStorageProperty(
                "unit", when (luminosityData.unit) {
                    LUnit.LUMENS -> LUnit.LUMENS.ordinal
                    LUnit.LUX -> LUnit.LUX.ordinal
                }
            )
        }
    }

    override fun load(
        element: StorageElement
    ): Outcome<LuminosityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                LUnit.LUMENS.ordinal -> LUnit.LUMENS
                LUnit.LUX.ordinal -> LUnit.LUX
                else -> null
            }
            if (unit == null) {
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "LUnit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    LuminosityData(
                        getStorageProperty("lum"),
                        unit
                    )
                )
            }
        }
}