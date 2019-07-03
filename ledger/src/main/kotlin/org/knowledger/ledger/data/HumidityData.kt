package org.knowledger.ledger.data

import com.squareup.moshi.JsonClass
import org.knowledger.common.config.LedgerConfiguration
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.SelfInterval
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.bytes
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