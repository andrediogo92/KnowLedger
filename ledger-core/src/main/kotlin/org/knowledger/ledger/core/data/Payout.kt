@file:UseSerializers(BigDecimalSerializer::class)

package org.knowledger.ledger.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.BigDecimalSerializer
import java.math.BigDecimal

/**
 * Payouts are a numeric representation
 * of the value of transactions.
 */
@Serializable
@SerialName("Payout")
data class Payout(
    val payout: BigDecimal
) {
    operator fun plus(
        payout: Payout
    ): Payout =
        Payout(payout.payout + this.payout)
}