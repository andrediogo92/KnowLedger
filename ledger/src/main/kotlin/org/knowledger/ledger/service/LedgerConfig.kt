package org.knowledger.ledger.service

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams

data class LedgerConfig(
    val ledgerId: LedgerId,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams
)