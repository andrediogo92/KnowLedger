package org.knowledger.ledger.data

import org.knowledger.ledger.core.data.ConvertableUnit
import org.knowledger.ledger.core.data.PhysicalUnit
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
enum class HUnit : PhysicalUnit,
                   ConvertableUnit<BigDecimal, HUnit> {
    RELATIVE {
        override fun convertTo(
            value: BigDecimal,
            to: HUnit
        ): BigDecimal = value
    },
    G_BY_KG {
        override fun convertTo(
            value: BigDecimal, to: HUnit
        ): BigDecimal = when (to) {
            RELATIVE, G_BY_KG -> value
            KG_BY_KG -> value.multiply(BigDecimal("1000"))
        }
    },
    KG_BY_KG {
        override fun convertTo(
            value: BigDecimal, to: HUnit
        ): BigDecimal = when (to) {
            RELATIVE, KG_BY_KG -> value
            G_BY_KG -> value.divide(
                BigDecimal("1000"),
                PhysicalData.MATH_CONTEXT
            )
        }
    };

}
