package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Coinbase
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes
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