package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable

interface LedgerStorageAdapter<T : LedgerContract> : StorageLoadable<T>,
                                                     Storable<T>,
                                                     SchemaProvider<T>