package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.common.storage.results.DataFailure
import pt.um.masb.ledger.data.NUnit
import pt.um.masb.ledger.data.NoiseData

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
        toStore: BlockChainData, session: NewInstanceSession
    ): StorageElement {
        val noiseData = toStore as NoiseData
        return session.newInstance(id).apply {
            setStorageProperty("noiseLevel", noiseData.noiseLevel)
            setStorageProperty("peakOrBase", noiseData.peakOrBase)
            setStorageProperty(
                "unit", when (noiseData.unit) {
                    NUnit.DBSPL -> NUnit.DBSPL.ordinal.toByte()
                    NUnit.RMS -> NUnit.RMS.ordinal.toByte()
                }
            )
        }
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
                Outcome.Error<NoiseData, DataFailure>(
                    DataFailure.UnrecognizedUnit(
                        "Unit is not one of the expected: $prop"
                    )
                )
            } else {
                Outcome.Ok<NoiseData, DataFailure>(
                    NoiseData(
                        getStorageProperty("noiseLevel"),
                        getStorageProperty("peakOrBase"),
                        unit
                    )
                )
            }
        }

}