package org.knowledger.ledger.core.adapters

import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.database.adapters.Loadable
import org.knowledger.ledger.database.adapters.Storable

/**
 * Main contract describing an object capable of, loading, storing
 * and deriving a schema for a given [LedgerData].
 */
internal interface StorageAdapter<T : LedgerData> : Loadable<T>, Storable<LedgerData>,
                                                    HashSchemaProvider