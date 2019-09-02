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
 * Luminosity value might be output by an ambient light
 * sensor, using lux units ([LuminosityUnit.LUX]) or a lighting unit,
 * outputting a specific amount of lumens ([LuminosityUnit.LUMENS]),
 * according to [unit].
 */
@Serializable
@SerialName("LuminosityData")
data class LuminosityData(
    val luminosity: BigDecimal,
    val unit: LuminosityUnit
) : LedgerData {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        when (previous) {
            is LuminosityData -> calculateDiffLum(previous)
            else -> throw InvalidClassException(
                """SelfInterval supplied is:
                    |   ${previous.javaClass.name},
                    |   not ${this::class.java.name}
                """.trimMargin()
            )
        }

    private fun calculateDiffLum(
        previous: LuminosityData
    ): BigDecimal =
        luminosity.subtract(previous.luminosity)
            .divide(
                previous.luminosity,
                GLOBALCONTEXT
            )


    override fun toString(): String {
        return "LuminosityData(lum = $luminosity, unit = $unit)"
    }
}
