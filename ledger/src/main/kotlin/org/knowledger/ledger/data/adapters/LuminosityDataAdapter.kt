package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.AvailableHashAlgorithms
import org.knowledger.ledger.data.LuminosityData
import org.knowledger.ledger.data.LuminosityUnit

object LuminosityDataAdapter : AbstractStorageAdapter<LuminosityData>(
    LuminosityData::class.java,
    AvailableHashAlgorithms.SHA3512Hasher
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "luminosity" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as LuminosityData).let {
            session
                .newInstance(id)
                .setStorageProperty("luminosity", it.luminosity)
                .setStorageProperty(
                    "unit", when (it.unit) {
                        LuminosityUnit.Lumens -> LuminosityUnit.Lumens.ordinal
                        LuminosityUnit.Lux -> LuminosityUnit.Lux.ordinal
                    }
                )
        }

    override fun load(
        element: StorageElement
    ): Outcome<LuminosityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                LuminosityUnit.Lumens.ordinal -> LuminosityUnit.Lumens
                LuminosityUnit.Lux.ordinal -> LuminosityUnit.Lux
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
                        getStorageProperty("luminosity"),
                        unit
                    )
                )
            }
        }
}