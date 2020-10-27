package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.config.ledger.factory.LedgerParamsFactory

internal data class LedgerMagicPair(
    val adapter: HandleStorageAdapter<LedgerParams>,
    val factory: LedgerParamsFactory,
)
