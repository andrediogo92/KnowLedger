package org.knowledger.ledger.storage.config.ledger

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.StoragePairs

internal class StorageAwareLedgerParamsImpl(
    override val ledgerParams: LedgerParams,
) : LedgerParams by ledgerParams, StorageAwareLedgerParams {
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = emptyArray()


    override fun equals(other: Any?): Boolean = ledgerParams == other

    override fun hashCode(): Int = ledgerParams.hashCode()
}