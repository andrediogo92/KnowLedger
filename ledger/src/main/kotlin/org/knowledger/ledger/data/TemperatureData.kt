package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.ledger.core.config.LedgerConfiguration
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.bytes
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
