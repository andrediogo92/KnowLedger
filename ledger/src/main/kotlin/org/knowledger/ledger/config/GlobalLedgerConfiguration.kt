package org.knowledger.ledger.config

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

object GlobalLedgerConfiguration {
    const val CACHE_SIZE = 40
    val GLOBALCONTEXT =
        MathContext(15, RoundingMode.HALF_EVEN)
    val RECALC_DIV =
        BigInteger("10000000000000")
    val RECALC_MULT =
        BigDecimal("10000000000000")
}