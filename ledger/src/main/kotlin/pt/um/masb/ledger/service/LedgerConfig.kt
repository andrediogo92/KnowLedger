package pt.um.masb.ledger.service

import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams

data class LedgerConfig(
    val ledgerId: LedgerId,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams
)