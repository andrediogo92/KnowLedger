package org.knowledger.ledger.service

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.service.transactions.PersistenceWrapper

internal data class LedgerContainer(
    val ledgerHash: Hash,
    val hasher: Hasher,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams,
    val persistenceWrapper: PersistenceWrapper,
    val formula: DataFormula
)