package org.knowledger.ledger.service.adapters

import org.knowledger.common.storage.adapters.SchemaProvider
import org.knowledger.common.storage.adapters.Storable
import org.knowledger.ledger.service.ServiceClass

interface ServiceStorageAdapter<T : ServiceClass> : ServiceLoadable<T>,
                                                    Storable<T>,
                                                    SchemaProvider<T>