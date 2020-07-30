package org.knowledger.ledger.adapters

import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Failure

internal interface StorageAdapter<T, out R : Failure> : Loadable<T, R>, EagerStorable<T>,
                                                        SchemaProvider