package org.knowledger.ledger.core.data

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.serial.PayoutSerializer
import java.math.BigDecimal

/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
@Serializable(with = PayoutSerializer::class)
data class Payout(
    val payout: BigDecimal
) {
    operator fun plus(
        payout: Payout
    ): Payout =
        Payout(payout.payout + this.payout)
}