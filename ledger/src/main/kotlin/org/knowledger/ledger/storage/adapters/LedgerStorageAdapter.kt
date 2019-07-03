package org.knowledger.ledger.storage.adapters

import org.knowledger.common.storage.LedgerContract
import org.knowledger.common.storage.adapters.SchemaProvider
import org.knowledger.common.storage.adapters.Storable

interface LedgerStorageAdapter<T : LedgerContract> : StorageLoadable<T>,
                                                     Storable<T>,
                                                     SchemaProvider<T>