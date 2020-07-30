package org.knowledger.ledger.adapters

import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.results.LoadFailure


internal interface LedgerStorageAdapter<T : LedgerContract> : StorageAdapter<T, LoadFailure>,
                                                              StorageLoadable<T>