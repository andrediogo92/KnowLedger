package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Coinbase
import java.math.BigDecimal


/**
 * Humidity data can be expressed in Absolute/Volumetric or Relative humidity.
 * As such possible measurements can be in g/kg, Kg/kg or percentage.
 */
class HumidityData(
        val hum: BigDecimal,
        val unit: HUnit
) : BlockChainData<HumidityData> {

    override fun calculateDiff(previous: HumidityData): BigDecimal {
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


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HumidityData) return false

        if (hum != other.hum) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hum.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "HumidityData(hum=$hum, unit=$unit)"
    }

}