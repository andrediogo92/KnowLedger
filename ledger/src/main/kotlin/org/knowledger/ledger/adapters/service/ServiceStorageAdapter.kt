package org.knowledger.ledger.adapters.service

import org.knowledger.ledger.adapters.StorageAdapter
import org.knowledger.ledger.chain.ServiceClass

internal interface ServiceStorageAdapter<T : ServiceClass> : StorageAdapter<T>, ServiceLoadable<T>
