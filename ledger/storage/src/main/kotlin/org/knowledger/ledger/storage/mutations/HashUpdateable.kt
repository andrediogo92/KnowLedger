package org.knowledger.ledger.storage.mutations

import org.knowledger.ledger.crypto.Hash

interface HashUpdateable {
    fun updateHash(hash: Hash)
}