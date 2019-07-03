package org.knowledger.common.storage.adapters

import org.knowledger.common.data.LedgerData

/**
 * Main contract describing an object capable of, loading, storing
 * and deriving a schema for a given [LedgerData].
 */
internal interface StorageAdapter<T : LedgerData> : Loadable<T>,
                                                    Storable<LedgerData>,
                                                    SchemaProvider<T>