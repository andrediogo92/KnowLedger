package pt.um.masb.common.storage.adapters

import pt.um.masb.common.data.LedgerData

/**
 * Main contract describing an object capable of, loading, storing
 * and deriving a schema for a given [LedgerData].
 */
internal interface StorageAdapter<T : LedgerData> : Loadable<T>,
                                                    Storable<LedgerData>,
                                                    SchemaProvider<T>