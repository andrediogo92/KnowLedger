package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.ConvertableUnit
import org.knowledger.ledger.core.data.PhysicalUnit
import org.knowledger.ledger.storage.config.GlobalLedgerConfiguration.GLOBALCONTEXT
import java.math.BigDecimal

/**
 * Three Humidity units considered:
 *
 * Relative humidity represents a percentage of water particles
 * from maximum quantity possible for the current atmospheric pressure.
 *
 * G/KG and KG/KG are absolute humidity measurements representing a mass of water per
 * mass of material measured.
 */
enum class HumidityUnit : PhysicalUnit, ConvertableUnit<BigDecimal, HumidityUnit> {
    Relative {
        override fun convertTo(value: BigDecimal, to: HumidityUnit): BigDecimal = value
    },
    GramsByKilograms {
        override fun convertTo(value: BigDecimal, to: HumidityUnit): BigDecimal =
            when (to) {
                Relative, GramsByKilograms -> value
                KilogramsByKilograms -> value.multiply(BigDecimal("1000"))
            }
    },
    KilogramsByKilograms {
        override fun convertTo(value: BigDecimal, to: HumidityUnit): BigDecimal =
            when (to) {
                Relative, KilogramsByKilograms -> value
                GramsByKilograms -> value.divide(BigDecimal("1000"), GLOBALCONTEXT)
            }
    };

}
