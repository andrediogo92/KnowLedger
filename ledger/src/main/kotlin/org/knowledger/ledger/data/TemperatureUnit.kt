package org.knowledger.ledger.data

import org.knowledger.ledger.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import org.knowledger.ledger.core.base.data.ConvertableUnit
import org.knowledger.ledger.core.base.data.PhysicalUnit
import java.math.BigDecimal


/**
 * Temperature Units and conversions between them
 * (Kelvin, Celsius, Fahrenheit and Rankine).
 */
enum class TemperatureUnit : PhysicalUnit,
                             ConvertableUnit<BigDecimal, TemperatureUnit> {
    Kelvin {
        override fun convertTo(
            value: BigDecimal, to: TemperatureUnit
        ): BigDecimal =
            when (to) {
                Kelvin -> value

                Celsius -> value.subtract(BigDecimal("273.16"))

                Rankine -> BigDecimal("9")
                    .multiply(value.subtract(BigDecimal("273.15")))
                    .divide(BigDecimal("5"))
                    .add(BigDecimal("491.69"))

                Fahrenheit -> BigDecimal("9")
                    .multiply(value.subtract(BigDecimal("273.15")))
                    .divide(
                        BigDecimal("5.0"),
                        GLOBALCONTEXT
                    )
                    .add(BigDecimal("32"))
            }
    },
    Celsius {
        override fun convertTo(
            value: BigDecimal, to: TemperatureUnit
        ): BigDecimal =
            when (to) {
                Kelvin -> value.add(BigDecimal("273.15"))

                Celsius -> value

                Rankine -> BigDecimal("9")
                    .multiply(
                        value.divide(
                            BigDecimal("5"),
                            GLOBALCONTEXT
                        )
                    )
                    .add(BigDecimal("491.69"))

                Fahrenheit -> BigDecimal("9")
                    .multiply(value.divide(BigDecimal("5")))
                    .add(BigDecimal("32"))
            }
    },
    Rankine {
        override fun convertTo(
            value: BigDecimal, to: TemperatureUnit
        ): BigDecimal =
            when (to) {
                Kelvin -> BigDecimal("5")
                    .multiply(value.subtract(BigDecimal("491.69")))
                    .divide(
                        BigDecimal("9"),
                        GLOBALCONTEXT
                    )
                    .add(BigDecimal("273.15"))

                Celsius -> BigDecimal("5")
                    .multiply(value.subtract(BigDecimal("491.69")))
                    .divide(
                        BigDecimal("9"),
                        GLOBALCONTEXT
                    )

                Rankine -> value

                Fahrenheit -> value.subtract(BigDecimal("459.69"))
            }
    },
    Fahrenheit {
        override fun convertTo(
            value: BigDecimal, to: TemperatureUnit
        ): BigDecimal =
            when (to) {
                Kelvin -> BigDecimal("5")
                    .multiply(value.subtract(BigDecimal("32")))
                    .divide(
                        BigDecimal("9"),
                        GLOBALCONTEXT
                    )
                    .add(BigDecimal("273.15"))

                Celsius -> BigDecimal("5")
                    .multiply(value.subtract(BigDecimal("32")))
                    .divide(
                        BigDecimal("9"),
                        GLOBALCONTEXT
                    )

                Rankine -> value.add(BigDecimal("459.69"))

                Fahrenheit -> value
            }
    };

}
