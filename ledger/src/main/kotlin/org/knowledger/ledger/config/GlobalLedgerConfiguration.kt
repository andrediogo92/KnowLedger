package org.knowledger.ledger.config

import kotlinx.serialization.cbor.Cbor
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode

object GlobalLedgerConfiguration {
    const val CACHE_SIZE = 40L
    val GLOBALCONTEXT =
        MathContext(15, RoundingMode.HALF_EVEN)
    val RECALC_DIV =
        BigInteger("10000000000000")
    val RECALC_MULT =
        BigDecimal("10000000000000")
    val DEFAULT_ENCODER = Cbor.plain
}