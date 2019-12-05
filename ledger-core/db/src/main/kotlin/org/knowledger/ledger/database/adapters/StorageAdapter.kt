package org.knowledger.ledger.database.adapters

import org.knowledger.ledger.core.base.data.LedgerData

/**
 * Main contract describing an object capable of, loading, storing
 * and deriving a schema for a given [LedgerData].
 */
internal interface StorageAdapter<T : LedgerData> : Loadable<T>,
                                                    Storable<LedgerData>,
                                                    SchemaProvider<T>