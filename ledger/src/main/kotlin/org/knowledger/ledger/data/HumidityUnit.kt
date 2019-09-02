package org.knowledger.ledger.data

import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.GLOBALCONTEXT
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
enum class HumidityUnit : PhysicalUnit,
                          ConvertableUnit<BigDecimal, HumidityUnit> {
    Relative {
        override fun convertTo(
            value: BigDecimal,
            to: HumidityUnit
        ): BigDecimal = value
    },
    G_By_KG {
        override fun convertTo(
            value: BigDecimal, to: HumidityUnit
        ): BigDecimal = when (to) {
            Relative, G_By_KG -> value
            KG_By_KG -> value.multiply(BigDecimal("1000"))
        }
    },
    KG_By_KG {
        override fun convertTo(
            value: BigDecimal, to: HumidityUnit
        ): BigDecimal = when (to) {
            Relative, KG_By_KG -> value
            G_By_KG -> value.divide(
                BigDecimal("1000"),
                GLOBALCONTEXT
            )
        }
    };

}
