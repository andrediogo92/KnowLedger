package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.database.adapters.SchemaProvider

internal interface HandleStorageAdapter<T> : HandleLoadable<T>, EagerStorable<T>, SchemaProvider