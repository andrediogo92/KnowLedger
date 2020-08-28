package org.knowledger.ledger.storage.config.ledger.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.LedgerParams

interface LedgerParamsFactory : CloningFactory<LedgerParams> {
    fun create(
        hasher: Hash, recalculationTime: Long = 1228800000, recalculationTrigger: Int = 2048,
    ): LedgerParams
}