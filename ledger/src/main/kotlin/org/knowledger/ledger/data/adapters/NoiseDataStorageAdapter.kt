package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.data.NUnit
import org.knowledger.ledger.data.NoiseData

object NoiseDataStorageAdapter : AbstractStorageAdapter<NoiseData>(
    NoiseData::class.java
) {
    override val properties: Map<String, StorageType>
        get() = mapOf(
            "noiseLevel" to StorageType.DECIMAL,
            "peakOrBase" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(
        toStore: LedgerData, session: NewInstanceSession
    ): StorageElement =
        (toStore as NoiseData).let {
            session
                .newInstance(id)
                .setStorageProperty("noiseLevel", it.noiseLevel)
                .setStorageProperty("peakOrBase", it.peakOrBase)
                .setStorageProperty(
                    "unit", when (it.unit) {
                        NUnit.DBSPL -> NUnit.DBSPL.ordinal.toByte()
                        NUnit.RMS -> NUnit.RMS.ordinal.toByte()
                    }
                )
        }


    override fun load(
        element: StorageElement
    ): Outcome<NoiseData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                NUnit.DBSPL.ordinal -> NUnit.DBSPL
                NUnit.RMS.ordinal -> NUnit.RMS
                else -> null
            }
            if (unit == null) {
                Outcome.Error<DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "Unit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok(
                    NoiseData(
                        getStorageProperty("noiseLevel"),
                        getStorageProperty("peakOrBase"),
                        unit
                    )
                )
            }
        }

}