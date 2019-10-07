package org.knowledger.ledger.service

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Hashers
import org.knowledger.ledger.service.transactions.PersistenceWrapper

internal data class LedgerContainer(
    val ledgerHash: Hash,
    val hasher: Hashers,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams,
    val serialModule: SerialModule,
    val persistenceWrapper: PersistenceWrapper,
    val formula: DataFormula,
    val encoder: BinaryFormat
)