package org.knowledger.ledger.storage.witness

import org.knowledger.ledger.storage.StorageAware

internal interface StorageAwareWitness : MutableHashedWitness, StorageAware {
    val witness: MutableHashedWitness
}