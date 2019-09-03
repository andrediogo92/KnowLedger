package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.HumidityData
import org.knowledger.ledger.data.HumidityUnit

object HumidityDataStorageAdapter : AbstractStorageAdapter<HumidityData>(
    HumidityData::class.java,
    Hashers.SHA3512Hasher
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "humidity" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as HumidityData).let {
            session
                .newInstance(id)
                .setStorageProperty("humidity", it.humidity)
                .setStorageProperty(
                    "unit", when (it.unit) {
                        HumidityUnit.G_By_KG -> HumidityUnit.G_By_KG.ordinal
                        HumidityUnit.KG_By_KG -> HumidityUnit.KG_By_KG.ordinal
                        HumidityUnit.Relative -> HumidityUnit.Relative.ordinal
                    }
                )
        }

    override fun load(
        element: StorageElement
    ): Outcome<HumidityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                HumidityUnit.G_By_KG.ordinal -> HumidityUnit.G_By_KG
                HumidityUnit.KG_By_KG.ordinal -> HumidityUnit.KG_By_KG
                HumidityUnit.Relative.ordinal -> HumidityUnit.Relative
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
                        getStorageProperty("humidity"),
                        unit
                    )
                )
            }
        }

}