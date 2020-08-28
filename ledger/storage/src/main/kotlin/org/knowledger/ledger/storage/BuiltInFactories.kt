package org.knowledger.ledger.storage

val suFactories: Factories by lazy { StorageUnawareFactories() }
val saFactories: Factories by lazy { StorageAwareFactories() }