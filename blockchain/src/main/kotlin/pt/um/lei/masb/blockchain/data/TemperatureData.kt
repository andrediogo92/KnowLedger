package pt.um.lei.masb.blockchain.data

import pt.um.lei.masb.blockchain.Coinbase
import java.math.BigDecimal

/**
 * Temperature data specifies a double and a Temperature unit (Celsius, Fahrenheit, Rankine and Kelvin) with
 * idempotent methods to convert between them as needed.
 * <pw>
 */
class TemperatureData(
        val temperature: BigDecimal,
        val unit: TUnit
) : BlockChainData<TemperatureData> {

    override fun calculateDiff(previous: TemperatureData): BigDecimal {
        val oldT = previous.unit.convertTo(previous.temperature, TUnit.CELSIUS)
        return unit.convertTo(temperature, TUnit.CELSIUS)
            .subtract(oldT)
            .divide(oldT, Coinbase.MATH_CONTEXT)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemperatureData) return false

        if (temperature != other.temperature) return false
        if (unit != other.unit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = temperature.hashCode()
        result = 31 * result + unit.hashCode()
        return result
    }

    override fun toString(): String {
        return "TemperatureData(temperature=$temperature, unit=$unit)"
    }


}
