package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.storage.adapters.SchemaProvider
import org.knowledger.ledger.core.storage.adapters.Storable
import org.knowledger.ledger.service.ServiceClass

interface ServiceStorageAdapter<T : ServiceClass> : ServiceLoadable<T>,
                                                    Storable<T>,
                                                    SchemaProvider<T>