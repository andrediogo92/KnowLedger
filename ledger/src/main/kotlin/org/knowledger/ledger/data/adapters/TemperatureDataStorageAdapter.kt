package org.knowledger.ledger.data.adapters

import kotlinx.serialization.KSerializer
import org.knowledger.ledger.core.adapters.AbstractStorageAdapter
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.TemperatureData
import org.knowledger.ledger.data.TemperatureUnit
import org.knowledger.ledger.database.NewInstanceSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.LedgerData
import java.math.BigDecimal

class TemperatureDataStorageAdapter(hashers: Hashers) :
    AbstractStorageAdapter<TemperatureData>(TemperatureData::class, hashers) {
    override val serializer: KSerializer<TemperatureData> get() = TemperatureData.serializer()

    override val properties: Map<String, StorageType>
        get() = mapOf("temperature" to StorageType.DECIMAL, "unit" to StorageType.INTEGER)

    override fun store(toStore: LedgerData, session: NewInstanceSession): StorageElement =
        (toStore as TemperatureData).let { temperatureData ->
            val unit = when (temperatureData.unit) {
                TemperatureUnit.Celsius -> TemperatureUnit.Celsius.ordinal
                TemperatureUnit.Fahrenheit -> TemperatureUnit.Fahrenheit.ordinal
                TemperatureUnit.Kelvin -> TemperatureUnit.Kelvin.ordinal
                TemperatureUnit.Rankine -> TemperatureUnit.Rankine.ordinal
            }
            session.newInstance(id)
                .setStorageProperty("temperature", temperatureData.temperature)
                .setStorageProperty("unit", unit)
        }


    override fun load(element: StorageElement): Outcome<TemperatureData, DataFailure> =
        commonLoad(element, id) {
            val prop = getStorageProperty<Int>("unit")
            val unit = when (prop) {
                TemperatureUnit.Celsius.ordinal -> TemperatureUnit.Celsius
                TemperatureUnit.Fahrenheit.ordinal -> TemperatureUnit.Fahrenheit
                TemperatureUnit.Kelvin.ordinal -> TemperatureUnit.Kelvin
                TemperatureUnit.Rankine.ordinal -> TemperatureUnit.Rankine
                else -> null
            }
            val temperature = getStorageProperty<BigDecimal>("temperature")
            unit?.let { unit1 -> TemperatureData(temperature, unit1).ok() }
            ?: DataFailure.UnrecognizedUnit("Unit is not one of the expected: $prop").err()
        }

}