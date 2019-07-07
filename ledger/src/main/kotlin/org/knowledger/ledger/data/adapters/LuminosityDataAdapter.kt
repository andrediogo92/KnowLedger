package org.knowledger.ledger.data.adapters

import org.knowledger.common.data.LedgerData
import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.results.Outcome
import org.knowledger.common.storage.adapters.AbstractStorageAdapter
import org.knowledger.common.storage.results.DataFailure
import org.knowledger.ledger.data.LUnit
import org.knowledger.ledger.data.LuminosityData

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
    ): StorageElement =
        (toStore as LuminosityData).let {
            session
                .newInstance(id)
                .setStorageProperty("lum", it.lum)
                .setStorageProperty(
                    "unit", when (it.unit) {
                        LUnit.LUMENS -> LUnit.LUMENS.ordinal
                        LUnit.LUX -> LUnit.LUX.ordinal
                    }
                )
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