package org.knowledger.ledger.core.base.data

import java.math.BigDecimal

/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
data class Payout(
    val payout: BigDecimal
) {
    operator fun plus(
        payout: Payout
    ): Payout =
        Payout(payout.payout + this.payout)
}