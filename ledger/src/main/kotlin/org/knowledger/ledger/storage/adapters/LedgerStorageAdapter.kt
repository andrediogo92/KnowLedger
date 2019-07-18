package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.core.storage.adapters.SchemaProvider
import org.knowledger.ledger.core.storage.adapters.Storable


interface LedgerStorageAdapter<T : LedgerContract> : StorageLoadable<T>,
                                                     Storable<T>,
                                                     SchemaProvider<T>