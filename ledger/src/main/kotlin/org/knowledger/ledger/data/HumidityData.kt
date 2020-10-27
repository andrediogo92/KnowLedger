@file:UseSerializers(BigDecimalSerializer::class)

package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.SelfInterval
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import java.io.InvalidClassException
import java.math.BigDecimal


/**
 * Humidity value can be expressed in Absolute/Volumetric
 * or Relative humidity. As such possible measurements
 * can be in g/Kg ([HumidityUnit.GramsByKilograms]), Kg/Kg ([HumidityUnit.KilogramsByKilograms])
 * or percentage ([HumidityUnit.Relative]) expressed by the [unit].
 */
@Serializable
@SerialName("HumidityData")
data class HumidityData(val humidity: BigDecimal, val unit: HumidityUnit) : LedgerData {
    override fun clone(): HumidityData = copy()

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(serializer(), this)

    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        when (previous) {
            is HumidityData -> calculateDiffHum(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }

    private fun calculateDiffHum(previous: HumidityData): BigDecimal {
        val newH: BigDecimal
        val oldH: BigDecimal
        if (unit == HumidityUnit.Relative) {
            newH = humidity
            oldH = previous.humidity
        } else {
            newH = unit.convertTo(humidity, HumidityUnit.KilogramsByKilograms)
            oldH = previous.unit.convertTo(humidity, HumidityUnit.KilogramsByKilograms)
        }
        return newH.subtract(oldH).divide(oldH, GLOBALCONTEXT)
    }
}