package org.knowledger.ledger.adapters

import org.knowledger.ledger.storage.LedgerContract


internal interface LedgerStorageAdapter<T : LedgerContract> : StorageAdapter<T>, StorageLoadable<T>