package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.Coinbase
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.io.InvalidClassException
import java.math.BigDecimal


/**
 * Humidity data can be expressed in Absolute/Volumetric
 * or Relative humidity. As such possible measurements
 * can be in g/Kg ([HUnit.G_BY_KG]), Kg/Kg ([HUnit.KG_BY_KG])
 * or percentage ([HUnit.RELATIVE]) expressed by the [unit].
 */
data class HumidityData(
    val hum: BigDecimal,
    val unit: HUnit
) : BlockChainData {
    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $hum
            ${unit.name}
            ${unit.ordinal}
            """.trimIndent()
        )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Humidity")
            .let {
                it.setProperty("hum", hum)
                val hUnit = when (unit) {
                    HUnit.G_BY_KG -> 0x00.toByte()
                    HUnit.KG_BY_KG -> 0x01.toByte()
                    HUnit.RELATIVE -> 0x02.toByte()
                }
                it.setProperty("unit", hUnit)
                it
            }


    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        when (previous) {
            is HumidityData ->
                calculateDiffHum(previous)
            else ->
                throw InvalidClassException("SelfInterval supplied is not ${this::class.simpleName}")
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


    override fun toString(): String {
        return "HumidityData(hum = $hum, unit = $unit)"
    }

}