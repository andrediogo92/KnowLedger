package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.core.storage.adapters.SchemaProvider


internal interface LedgerStorageAdapter<T : LedgerContract> : StorageLoadable<T>,
                                                              EagerStorable<T>,
                                                              SchemaProvider<T>