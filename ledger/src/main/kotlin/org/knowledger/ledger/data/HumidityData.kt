@file:UseSerializers(BigDecimalSerializer::class)
package org.knowledger.ledger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import java.io.InvalidClassException
import java.math.BigDecimal


/**
 * Humidity value can be expressed in Absolute/Volumetric
 * or Relative humidity. As such possible measurements
 * can be in g/Kg ([HumidityUnit.G_By_KG]), Kg/Kg ([HumidityUnit.KG_By_KG])
 * or percentage ([HumidityUnit.Relative]) expressed by the [unit].
 */
@Serializable
@SerialName("HumidityData")
data class HumidityData(
    val humidity: BigDecimal,
    val unit: HumidityUnit
) : LedgerData {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

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
            newH = unit.convertTo(humidity, HumidityUnit.KG_By_KG)
            oldH = previous.unit.convertTo(humidity, HumidityUnit.KG_By_KG)
        }
        return newH.subtract(oldH)
            .divide(oldH, GLOBALCONTEXT)
    }

    override fun toString(): String {
        return "HumidityData(hum=$humidity, unit=$unit)"
    }

}