package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.storage.cache.StorageAware

internal interface StorageAwareWitness : MutableHashedWitness, StorageAware {
    val witness: MutableHashedWitness
}