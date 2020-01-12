package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.adapters.EagerStorable
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.service.ServiceClass

internal interface ServiceStorageAdapter<T : ServiceClass> : ServiceLoadable<T>,
                                                             EagerStorable<T>,
                                                             SchemaProvider