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
 * Temperature value specifies a decimal temperature value
 * and a Temperature unit ([TemperatureUnit.Celsius],
 * [TemperatureUnit.Fahrenheit], [TemperatureUnit.Rankine] and [TemperatureUnit.Kelvin])
 * with idempotent methods to convert between them as needed.
 */
@Serializable
@SerialName("TemperatureData")
data class TemperatureData(
    val temperature: BigDecimal,
    val unit: TemperatureUnit
) : LedgerData {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is TemperatureData -> calculateDiffTemp(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }


    private fun calculateDiffTemp(
        previous: TemperatureData
    ): BigDecimal {
        val oldT = previous.unit.convertTo(
            previous.temperature,
            TemperatureUnit.Celsius
        )
        return unit.convertTo(temperature, TemperatureUnit.Celsius)
            .subtract(oldT)
            .divide(oldT, GLOBALCONTEXT)
    }

}
