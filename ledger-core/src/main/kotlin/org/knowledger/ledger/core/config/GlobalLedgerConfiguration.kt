package org.knowledger.ledger.core.config

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

object GlobalLedgerConfiguration {
    val GLOBALCONTEXT =
        MathContext(15, RoundingMode.HALF_EVEN)
    const val CACHE_SIZE = 40L
    val RECALC_DIV =
        BigInteger("10000000000000")
    val RECALC_MULT =
        BigDecimal("10000000000000")
    const val OTHER_BASE = 40L
    const val DATA_BASE = 5L
}