package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import pt.um.masb.common.config.LedgerConfiguration
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
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
) : BlockChainData {
    override fun digest(c: Hasher): Hash =
        c.applyHash(
            hum.unscaledValue().toByteArray() + unit.ordinal.bytes()
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