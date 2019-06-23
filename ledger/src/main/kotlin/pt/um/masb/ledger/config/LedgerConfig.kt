package pt.um.masb.ledger.config

import pt.um.masb.common.storage.LedgerContract

data class LedgerConfig(
    val ledgerId: LedgerId,
    val ledgerParams: LedgerParams
) : LedgerContract {

}