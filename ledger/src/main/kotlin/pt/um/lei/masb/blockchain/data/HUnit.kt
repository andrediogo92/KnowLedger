package pt.um.lei.masb.blockchain.data

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
            HUnit.RELATIVE, HUnit.G_BY_KG -> value
            HUnit.KG_BY_KG -> value.multiply(BigDecimal("1000"))
        }
    },
    KG_BY_KG {
        override fun convertTo(
            value: BigDecimal, to: HUnit
        ): BigDecimal = when (to) {
            HUnit.RELATIVE, HUnit.KG_BY_KG -> value
            HUnit.G_BY_KG -> value.divide(BigDecimal("1000"), PhysicalData.MATH_CONTEXT)
        }
    };

}
