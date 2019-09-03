package org.knowledger.ledger.service

import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.service.transactions.PersistenceWrapper

internal data class LedgerContainer(
    val ledgerHash: Hash,
    val hasher: Hashers,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams,
    val serialModule: SerialModule,
    val persistenceWrapper: PersistenceWrapper,
    val formula: DataFormula,
    val cbor: Cbor
)