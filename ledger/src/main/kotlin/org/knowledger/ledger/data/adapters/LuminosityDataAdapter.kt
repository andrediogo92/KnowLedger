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
import java.math.BigDecimal

class LuminosityDataAdapter(hashers: Hashers) :
    AbstractStorageAdapter<LuminosityData>(LuminosityData::class, hashers) {
    override val serializer: KSerializer<LuminosityData> get() = LuminosityData.serializer()


    override val properties: Map<String, StorageType>
        get() = mapOf("luminosity" to StorageType.DECIMAL, "unit" to StorageType.INTEGER)

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as LuminosityData).let { luminosityData ->
            val unit = when (luminosityData.unit) {
                LuminosityUnit.Lumens -> LuminosityUnit.Lumens.ordinal
                LuminosityUnit.Lux -> LuminosityUnit.Lux.ordinal
            }
            session.newInstance(id)
                .setStorageProperty("luminosity", luminosityData.luminosity)
                .setStorageProperty("unit", unit)
        }

    override fun load(element: StorageElement): Outcome<LuminosityData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                LuminosityUnit.Lumens.ordinal -> LuminosityUnit.Lumens
                LuminosityUnit.Lux.ordinal -> LuminosityUnit.Lux
                else -> null
            }
            val luminosity = getStorageProperty<BigDecimal>("luminosity")
            unit?.let { unit1 -> LuminosityData(luminosity, unit1).ok() }
            ?: DataFailure.UnrecognizedUnit("LUnit is not one of the expected: $prop").err()
        }
}