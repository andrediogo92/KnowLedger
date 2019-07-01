package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.storage.adapters.SchemaProvider
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.service.ServiceClass

interface ServiceStorageAdapter<T : ServiceClass> : ServiceLoadable<T>,
                                                    Storable<T>,
                                                    SchemaProvider<T>