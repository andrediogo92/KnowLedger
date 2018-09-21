package pt.um.lei.masb.blockchain.data

import java.math.BigDecimal


/**
 * Temperature Units and conversions between them (Kelvin, Celsius, Fahrenheit and Rankine).
 */
enum class TUnit : PhysicalUnit, ConvertableUnit<BigDecimal, TUnit> {
    KELVIN {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal = when (to) {
            TUnit.KELVIN -> value
            TUnit.CELSIUS -> value.subtract(BigDecimal("273.16"))
            TUnit.RANKINE -> BigDecimal("9").multiply(value.subtract(BigDecimal("273.15")))
                .divide(BigDecimal("5"))
                .add(BigDecimal("491.69"))
            TUnit.FAHRENHEIT -> BigDecimal("9").multiply(value.subtract(BigDecimal("273.15")))
                .divide(BigDecimal("5.0"),
                        PhysicalData.MATH_CONTEXT)
                .add(BigDecimal("32"))
        }
    },
    CELSIUS {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal = when (to) {
            TUnit.KELVIN -> value.add(BigDecimal("273.15"))
            TUnit.CELSIUS -> value
            TUnit.RANKINE -> BigDecimal("9").multiply(value.divide(BigDecimal("5"),
                                                                   PhysicalData.MATH_CONTEXT))
                .add(BigDecimal("491.69"))
            TUnit.FAHRENHEIT -> BigDecimal("9").multiply(value.divide(BigDecimal("5")))
                .add(BigDecimal("32"))
        }
    },
    RANKINE {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal = when (to) {
            TUnit.KELVIN -> BigDecimal("5").multiply(value.subtract(BigDecimal("491.69")))
                .divide(BigDecimal("9"), PhysicalData.MATH_CONTEXT)
                .add(BigDecimal("273.15"))
            TUnit.CELSIUS -> BigDecimal("5").multiply(value.subtract(BigDecimal("491.69")))
                .divide(BigDecimal("9"),
                        PhysicalData.MATH_CONTEXT)
            TUnit.RANKINE -> value
            TUnit.FAHRENHEIT -> value.subtract(BigDecimal("459.69"))
        }
    },
    FAHRENHEIT {
        override fun convertTo(value: BigDecimal, to: TUnit): BigDecimal = when (to) {
            TUnit.KELVIN -> BigDecimal("5").multiply(value.subtract(BigDecimal("32")))
                .divide(BigDecimal("9"), PhysicalData.MATH_CONTEXT)
                .add(BigDecimal("273.15"))
            TUnit.CELSIUS -> BigDecimal("5").multiply(value.subtract(BigDecimal("32")))
                .divide(BigDecimal("9"),
                        PhysicalData.MATH_CONTEXT)
            TUnit.RANKINE -> value.add(BigDecimal("459.69"))
            TUnit.FAHRENHEIT -> value
        }
    };

}
