package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.NoiseData
import org.knowledger.ledger.data.NoiseUnit
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData
import java.math.BigDecimal

class NoiseDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<NoiseData>(NoiseData::class, hashers) {
    override val serializer: KSerializer<NoiseData> get() = NoiseData.serializer()

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "noiseLevel" to StorageType.DECIMAL,
            "peakOrBase" to StorageType.DECIMAL,
            "unit" to StorageType.INTEGER
        )

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as NoiseData).let { noiseData ->
            val unit = when (noiseData.unit) {
                NoiseUnit.dBSPL -> NoiseUnit.dBSPL.ordinal.toByte()
                NoiseUnit.Rms -> NoiseUnit.Rms.ordinal.toByte()
            }
            session.newInstance(id)
                .setStorageProperty("noiseLevel", noiseData.noiseLevel)
                .setStorageProperty("peakOrBase", noiseData.peakOrBase)
                .setStorageProperty("unit", unit)
        }


    override fun load(element: StorageElement): Outcome<NoiseData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                NoiseUnit.dBSPL.ordinal -> NoiseUnit.dBSPL
                NoiseUnit.Rms.ordinal -> NoiseUnit.Rms
                else -> null
            }
            val noiseLevel = getStorageProperty<BigDecimal>("noiseLevel")
            val peakOrBase = getStorageProperty<BigDecimal>("peakOrBase")
            unit?.let { unit1 -> NoiseData(noiseLevel, peakOrBase, unit1).ok() }
            ?: DataFailure.UnrecognizedUnit("Unit is not one of the expected: $prop").err()
        }

}