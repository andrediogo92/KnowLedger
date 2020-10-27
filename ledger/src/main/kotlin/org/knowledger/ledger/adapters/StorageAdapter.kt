package org.knowledger.ledger.adapters

import org.knowledger.ledger.database.adapters.SchemaProvider

internal interface StorageAdapter<T> : Loadable<T>, EagerStorable<T>, SchemaProvider