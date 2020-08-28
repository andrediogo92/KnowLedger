package org.knowledger.ledger.storage.config.ledger.factory

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.LedgerParams
import org.knowledger.ledger.storage.config.ledger.StorageAwareLedgerParamsImpl

internal class StorageAwareLedgerParamsFactory(
    private val factory: LedgerParamsFactory = LedgerParamsFactoryImpl(),
) : LedgerParamsFactory {
    private fun createSA(ledgerParams: LedgerParams): StorageAwareLedgerParamsImpl =
        StorageAwareLedgerParamsImpl(ledgerParams)

    override fun create(
        hasher: Hash, recalculationTime: Long, recalculationTrigger: Int,
    ): StorageAwareLedgerParamsImpl =
        createSA(factory.create(hasher, recalculationTime, recalculationTrigger))

    override fun create(other: LedgerParams): StorageAwareLedgerParamsImpl =
        createSA(factory.create(other))
}