package pt.um.masb.ledger.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.misc.bytes
import pt.um.masb.ledger.Coinbase
import java.io.InvalidClassException
import java.math.BigDecimal


/**
 * Humidity data can be expressed in Absolute/Volumetric
 * or Relative humidity. As such possible measurements
 * can be in g/Kg ([HUnit.G_BY_KG]), Kg/Kg ([HUnit.KG_BY_KG])
 * or percentage ([HUnit.RELATIVE]) expressed by the [unit].
 */
@JsonClass(generateAdapter = true)
data class HumidityData(
    val hum: BigDecimal,
    val unit: HUnit
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            hum.unscaledValue().toByteArray() + unit.ordinal.bytes()
        )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Humidity")
            .apply {
                setProperty("hum", hum)
                val hUnit = when (unit) {
                    HUnit.G_BY_KG -> HUnit.G_BY_KG.ordinal
                    HUnit.KG_BY_KG -> HUnit.KG_BY_KG.ordinal
                    HUnit.RELATIVE -> HUnit.RELATIVE.ordinal
                }
                setProperty("unit", hUnit)
            }


    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        when (previous) {
            is HumidityData -> calculateDiffHum(previous)
            else -> throw InvalidClassException(
                "SelfInterval supplied is not ${this::class.java.name}"
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
            .divide(oldH, Coinbase.MATH_CONTEXT)
    }
}