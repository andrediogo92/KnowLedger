package org.knowledger.ledger.data.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.storage.adapters.AbstractStorageAdapter
import org.knowledger.ledger.core.storage.results.DataFailure
import org.knowledger.ledger.crypto.hash.AvailableHashAlgorithms
import org.knowledger.ledger.data.NoiseData
import org.knowledger.ledger.data.NoiseUnit

object NoiseDataStorageAdapter : AbstractStorageAdapter<NoiseData>(
    NoiseData::class.java,
    AvailableHashAlgorithms.SHA3512Hasher
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
                        NoiseUnit.dBSPL -> NoiseUnit.dBSPL.ordinal.toByte()
                        NoiseUnit.Rms -> NoiseUnit.Rms.ordinal.toByte()
                    }
                )
        }


    override fun load(
        element: StorageElement
    ): Outcome<NoiseData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                NoiseUnit.dBSPL.ordinal -> NoiseUnit.dBSPL
                NoiseUnit.Rms.ordinal -> NoiseUnit.Rms
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