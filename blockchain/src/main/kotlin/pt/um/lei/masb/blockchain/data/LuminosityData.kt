package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Coinbase
import java.math.BigDecimal

/**
 * Luminosity data might be output by an ambient light sensor, using lux units
 * or a lighting unit, outputting a specific amount of lumens.
 */
class LuminosityData(
        val lum: BigDecimal,
        val unit: LUnit
) : BlockChainData<LuminosityData> {

    override fun calculateDiff(previous: LuminosityData): BigDecimal =
            lum.subtract(previous.lum)
                .divide(previous.lum, Coinbase.MATH_CONTEXT)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LuminosityData) return false

        if (lum != other.lum) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lum.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "LuminosityData(lum=$lum, unit=$unit)"
    }
}
