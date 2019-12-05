package org.knowledger.ledger.service

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.service.transactions.PersistenceWrapper

data class LedgerInfo internal constructor(
    val ledgerId: LedgerId,
    val hasher: Hashers,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams,
    val serialModule: SerialModule,
    val formula: DataFormula,
    val encoder: BinaryFormat = Cbor(context = serialModule),
    internal val persistenceWrapper: PersistenceWrapper
)