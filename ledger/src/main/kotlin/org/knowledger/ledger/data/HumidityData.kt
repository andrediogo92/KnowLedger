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
 * Humidity value can be expressed in Absolute/Volumetric
 * or Relative humidity. As such possible measurements
 * can be in g/Kg ([HUnit.G_BY_KG]), Kg/Kg ([HUnit.KG_BY_KG])
 * or percentage ([HUnit.RELATIVE]) expressed by the [unit].
 */
@JsonClass(generateAdapter = true)
data class HumidityData(
    val hum: BigDecimal,
    val unit: HUnit
) : LedgerData {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            hum.bytes() + unit.ordinal.bytes()
        )


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
        if (unit == HUnit.RELATIVE) {
            newH = hum
            oldH = previous.hum
        } else {
            newH = unit.convertTo(hum, HUnit.KG_BY_KG)
            oldH = previous.unit.convertTo(hum, HUnit.KG_BY_KG)
        }
        return newH.subtract(oldH)
            .divide(oldH, LedgerConfiguration.GLOBALCONTEXT)
    }

    override fun toString(): String {
        return "HumidityData(hum=$hum, unit=$unit)"
    }

}