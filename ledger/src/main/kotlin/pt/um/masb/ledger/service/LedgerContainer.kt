package pt.um.masb.ledger.service

import pt.um.masb.common.data.DataFormula
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.ledger.config.CoinbaseParams
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

data class LedgerContainer(
    val ledgerHash: Hash,
    val hasher: Hasher,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams,
    val persistenceWrapper: PersistenceWrapper,
    val formula: DataFormula
)