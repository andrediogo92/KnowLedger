package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import pt.um.masb.common.config.LedgerConfiguration
import pt.um.masb.common.data.LedgerData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Temperature value specifies a decimal temperature value
 * and a Temperature unit ([TUnit.CELSIUS],
 * [TUnit.FAHRENHEIT], [TUnit.RANKINE] and [TUnit.KELVIN])
 * with idempotent methods to convert between them as needed.
 */
@JsonClass(generateAdapter = true)
data class TemperatureData(
    val temperature: BigDecimal,
    val unit: TUnit
) : LedgerData {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            temperature.bytes() + unit.ordinal.bytes()
        )

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
            TUnit.CELSIUS
        )
        return unit.convertTo(temperature, TUnit.CELSIUS)
            .subtract(oldT)
            .divide(oldT, LedgerConfiguration.GLOBALCONTEXT)
    }

}
