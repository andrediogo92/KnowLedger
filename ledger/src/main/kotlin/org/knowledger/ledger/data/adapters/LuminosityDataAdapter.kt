package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.LuminosityData
import org.knowledger.ledger.data.LuminosityUnit
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData

class LuminosityDataAdapter(hasher: Hashers) : AbstractStorageAdapter<LuminosityData>(
    LuminosityData::class.java, hasher
) {
    override val serializer: KSerializer<LuminosityData>
        get() = LuminosityData.serializer()


    override val properties: Map<String, StorageType>
        get() = mapOf(
            "luminosity" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement = (toStore as LuminosityData).let {
        session.newInstance(id)
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
                DataFailure.UnrecognizedUnit(
                    "LUnit is not one of the expected: $prop"
                ).err()
            } else {
                LuminosityData(
                    getStorageProperty("luminosity"), unit
                ).ok()
            }
        }
}