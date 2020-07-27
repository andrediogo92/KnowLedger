package org.knowledger.ledger.storage.config.ledger.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.config.ledger.ImmutableLedgerParams

internal class LedgerParamsFactoryImpl : LedgerParamsFactory {
    override fun create(
        hasher: Hash, recalculationTime: Long,
        recalculationTrigger: Int
    ): ImmutableLedgerParams = ImmutableLedgerParams(
        hasher, recalculationTime, recalculationTrigger
    )

    override fun create(other: LedgerParams): ImmutableLedgerParams =
        with(other) {
            create(hashers, recalculationTime, recalculationTrigger)
        }
}