package org.knowledger.ledger.core.data

import java.math.BigDecimal

/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
data class Payout(val payout: BigDecimal) : ByteEncodable {
    override val bytes: ByteArray get() = payout.unscaledValue().toByteArray()

    operator fun plus(payout: Payout): Payout =
        Payout(payout.payout + this.payout)

    override fun toString(): String = payout.toString()

    companion object {
        val ZERO = Payout(BigDecimal.ZERO)
        val ONE = Payout(BigDecimal.ONE)
    }
}