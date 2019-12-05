package org.knowledger.ledger.core.base.data

import java.math.BigDecimal
import java.math.MathContext

interface DataFormula {
    fun calculateDiff(
        base: Long,
        timeBase: Long,
        deltaTime: BigDecimal,
        valueBase: Long,
        deltaValue: BigDecimal,
        constant: Long,
        threshold: Long,
        mathContext: MathContext
    ): Payout
}