package org.knowledger.common.data

import java.math.BigDecimal

/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
inline class Payout(val payout: BigDecimal) {
    val bytes: ByteArray
        get() = payout.unscaledValue().toByteArray()

    fun add(payout: Payout): Payout =
        Payout(payout.payout.add(this.payout))

    fun contentEquals(payout: Payout): Boolean =
        this.payout == payout.payout

    fun contentHashCode(): Int =
        this.payout.hashCode()
}