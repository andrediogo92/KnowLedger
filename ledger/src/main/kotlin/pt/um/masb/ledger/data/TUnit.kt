package pt.um.masb.ledger.data

import pt.um.masb.common.data.ConvertableUnit
import pt.um.masb.common.data.PhysicalUnit
import java.math.BigDecimal


/**
 * Temperature Units and conversions between them
 * (Kelvin, Celsius, Fahrenheit and Rankine).
 */
enum class TUnit : PhysicalUnit, ConvertableUnit<BigDecimal, TUnit> {
    KELVIN {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal =
            when (to) {
                KELVIN -> value
                CELSIUS -> value.subtract(BigDecimal("273.16"))
                RANKINE -> BigDecimal("9").multiply(value.subtract(BigDecimal("273.15")))
                    .divide(BigDecimal("5"))
                    .add(BigDecimal("491.69"))
                FAHRENHEIT -> BigDecimal("9").multiply(value.subtract(BigDecimal("273.15")))
                    .divide(
                        BigDecimal("5.0"),
                        PhysicalData.MATH_CONTEXT
                    )
                    .add(BigDecimal("32"))
            }
    },
    CELSIUS {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal =
            when (to) {
                KELVIN -> value.add(BigDecimal("273.15"))
                CELSIUS -> value
                RANKINE -> BigDecimal("9").multiply(
                    value.divide(
                        BigDecimal("5"),
                        PhysicalData.MATH_CONTEXT
                    )
                )
                    .add(BigDecimal("491.69"))
                FAHRENHEIT -> BigDecimal("9").multiply(value.divide(BigDecimal("5")))
                    .add(BigDecimal("32"))
            }
    },
    RANKINE {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal =
            when (to) {
                KELVIN -> BigDecimal("5").multiply(value.subtract(BigDecimal("491.69")))
                    .divide(BigDecimal("9"), PhysicalData.MATH_CONTEXT)
                    .add(BigDecimal("273.15"))
                CELSIUS -> BigDecimal("5").multiply(value.subtract(BigDecimal("491.69")))
                    .divide(
                        BigDecimal("9"),
                        PhysicalData.MATH_CONTEXT
                    )
                RANKINE -> value
                FAHRENHEIT -> value.subtract(BigDecimal("459.69"))
            }
    },
    FAHRENHEIT {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal =
            when (to) {
                KELVIN -> BigDecimal("5").multiply(value.subtract(BigDecimal("32")))
                    .divide(BigDecimal("9"), PhysicalData.MATH_CONTEXT)
                    .add(BigDecimal("273.15"))
                CELSIUS -> BigDecimal("5").multiply(value.subtract(BigDecimal("32")))
                    .divide(
                        BigDecimal("9"),
                        PhysicalData.MATH_CONTEXT
                    )
                RANKINE -> value.add(BigDecimal("459.69"))
                FAHRENHEIT -> value
            }
    };

}
