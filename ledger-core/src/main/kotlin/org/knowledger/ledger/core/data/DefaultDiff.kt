package org.knowledger.ledger.core.data

import java.math.BigDecimal
import java.math.MathContext

object DefaultDiff : DataFormula {
    override fun calculateDiff(
        base: Long,
        timeBase: Long,
        deltaTime: BigDecimal,
        valueBase: Long,
        deltaValue: BigDecimal,
        constant: Long,
        threshold: Long,
        mathContext: MathContext
    ): Payout {
        val standardDivisor = BigDecimal(threshold * constant)
        val timeFactor = deltaTime
            .multiply(BigDecimal(timeBase))
            .pow(2, mathContext)
            .divide(standardDivisor, mathContext)

        val valueFactor = deltaValue
            .divide(BigDecimal(2), mathContext)
            .multiply(BigDecimal(valueBase))
            .divide(standardDivisor, mathContext)

        val baseFactor = BigDecimal(base).divide(standardDivisor, mathContext)
        return Payout(
            timeFactor
                .add(valueFactor)
                .add(baseFactor)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DefaultDiff) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}