package org.knowledger.ledger.service

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams

/**
 *
 */
@Serializable
@SerialName("LedgerConfig")
data class LedgerConfig(
    val ledgerId: LedgerId,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams
)

