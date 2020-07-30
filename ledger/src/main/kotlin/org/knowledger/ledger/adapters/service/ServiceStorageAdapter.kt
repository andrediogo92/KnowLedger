package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.StorageAdapter
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.storage.results.LedgerFailure

internal interface ServiceStorageAdapter<T : ServiceClass> : StorageAdapter<T, LedgerFailure>,
                                                             ServiceLoadable<T>
