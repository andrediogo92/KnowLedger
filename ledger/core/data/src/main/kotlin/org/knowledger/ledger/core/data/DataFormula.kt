package org.knowledger.ledger.core.data

import org.knowledger.ledger.core.data.hash.Hashing
import java.math.BigDecimal
import java.math.MathContext

interface DataFormula : Hashing, Comparable<DataFormula> {
    override fun compareTo(other: DataFormula): Int =
        hash.compareTo(other.hash)

    fun calculateDiff(
        base: Long, timeBase: Long, deltaTime: BigDecimal, valueBase: Long,
        deltaValue: BigDecimal, constant: Long, threshold: Long, mathContext: MathContext,
    ): Payout
}