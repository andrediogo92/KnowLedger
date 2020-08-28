package org.knowledger.ledger.storage.config.ledger

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareLedgerParams : LedgerParams, StorageAware {
    val ledgerParams: LedgerParams
}