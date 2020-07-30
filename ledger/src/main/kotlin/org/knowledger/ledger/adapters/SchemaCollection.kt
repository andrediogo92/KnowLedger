package org.knowledger.ledger.adapters

import org.knowledger.collections.SortedList
import org.knowledger.ledger.database.adapters.SchemaProvider

internal interface SchemaCollection : AdapterCollection, DataAdapters {
    @Suppress("UNCHECKED_CAST")
    val providers: SortedList<SchemaProvider>
        get() = defaultSchemas + (dataAdapters as SortedList<SchemaProvider>)
}