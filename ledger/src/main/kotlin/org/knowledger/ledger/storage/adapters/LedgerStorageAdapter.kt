package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.storage.LedgerContract


internal interface LedgerStorageAdapter<T : LedgerContract> : StorageLoadable<T>,
                                                              EagerStorable<T>,
                                                              SchemaProvider