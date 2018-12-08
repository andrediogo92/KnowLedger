package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ODocument
import kotlinx.serialization.Serializable
import pt.um.lei.masb.blockchain.Coinbase
import java.io.InvalidClassException
import java.math.BigDecimal

/**
 * Temperature data specifies a double and a Temperature unit (Celsius, Fahrenheit, Rankine and Kelvin) with
 * idempotent methods to convert between them as needed.
 * <pw>
 */
@Serializable
class TemperatureData(
    val temperature: BigDecimal,
    val unit: TUnit
) : BlockChainData {
    override fun store(): OElement =
        ODocument("Temperature").let {
            it.setProperty("temperature", temperature)
            it.setProperty(
                "unit", when (unit) {
                    TUnit.CELSIUS -> 0x00.toByte()
                    TUnit.FAHRENHEIT -> 0x01.toByte()
                    TUnit.KELVIN -> 0x02.toByte()
                    TUnit.RANKINE -> 0x03.toByte()
                }
            )
            it
        }

    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        when (previous) {
            is TemperatureData -> calculateDiffTemp(previous)
            else ->
                throw InvalidClassException("SelfInterval supplied is not ${this::class.simpleName}")
        }


    private fun calculateDiffTemp(previous: TemperatureData): BigDecimal {
        val oldT = previous.unit.convertTo(
            previous.temperature,
            TUnit.CELSIUS
        )
        return unit.convertTo(temperature, TUnit.CELSIUS)
            .subtract(oldT)
            .divide(oldT, Coinbase.MATH_CONTEXT)
    }

    override fun toString(): String =
        "TemperatureData(temperature=$temperature,unit=$unit)"


}
